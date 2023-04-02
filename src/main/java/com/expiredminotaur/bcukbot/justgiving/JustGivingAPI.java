package com.expiredminotaur.bcukbot.justgiving;

import com.expiredminotaur.bcukbot.HttpHandler;
import com.expiredminotaur.bcukbot.command.CommandEvent;
import com.expiredminotaur.bcukbot.discord.DiscordBot;
import com.expiredminotaur.bcukbot.discord.music.MusicHandler;
import com.expiredminotaur.bcukbot.twitch.TwitchBot;
import com.expiredminotaur.bcukbot.twitch.command.chat.TwitchCommandEvent;
import com.expiredminotaur.bcukbot.twitch.streams.LiveStreamManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class JustGivingAPI
{
    private final Logger log = LoggerFactory.getLogger(JustGivingAPI.class);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final TwitchBot twitchBot;
    private final DiscordBot discordBot;
    private final MusicHandler musicHandler;
    private final LiveStreamManager liveStreamManager;
    private final JustGivingSettings settings;
    private String data;
    private String totalRaisedMessage = null;
    private ScheduledFuture<?> task = null;

    public JustGivingAPI(@Lazy @Autowired TwitchBot twitchBot, @Lazy @Autowired DiscordBot discordBot, @Lazy @Autowired MusicHandler musicHandler, @Lazy @Autowired LiveStreamManager liveStreamManager)
    {
        this.twitchBot = twitchBot;
        this.discordBot = discordBot;
        this.musicHandler = musicHandler;
        this.liveStreamManager = liveStreamManager;
        JustGivingSettings settings;
        try (FileReader fr = new FileReader(JustGivingSettings.getFileName()))
        {
            settings = gson.fromJson(fr, JustGivingSettings.class);
        } catch (IOException e)
        {
            log.warn("Can't load just giving settings file, this may be because one has not been made yet");
            settings = new JustGivingSettings();
        }
        this.settings = settings;
        saveSettings();
    }

    public void saveSettings()
    {
        try (FileWriter fw = new FileWriter(JustGivingSettings.getFileName()))
        {
            gson.toJson(settings, fw);
        } catch (IOException e)
        {
            log.error("Unable to save just giving setting file", e);
        }
        updateScheduler();
    }

    private void updateScheduler()
    {
        if (settings.getAutoCheckEnabled() && task == null)
        {
            task = scheduler.scheduleAtFixedRate(this::checkForNewData, 0, 1, TimeUnit.SECONDS);
        } else if (!settings.getAutoCheckEnabled() && task != null)
        {
            task.cancel(false);
            task = null;
        }
    }

    private void checkForNewData()
    {
        try
        {
            URL url = new URL(String.format("https://api.justgiving.com/%s/v1/fundraising/pages/%s", settings.getAppId(), settings.getCampaignName()));
            BufferedReader br = HttpHandler.getRequest(url);
            String output;
            if (br != null && (output = br.readLine()) != null)
            {
                updateData(output);
            }
        } catch (Exception e)
        {
            log.error("Justgiving API error", e);
            try
            {
                //If the last check failed wait for 30 seconds before trying again
                Thread.sleep(30000);
            } catch (InterruptedException e2)
            {
                log.error("Justgiving Sleep Interrupted Exception", e2);
            }
        }
    }

    private void updateData(String data)
    {
        this.data = data;
        JsonElement jsonTree = JsonParser.parseString(data);
        if (jsonTree.isJsonObject())
        {
            JsonObject jsonObject = jsonTree.getAsJsonObject();
            double total = Double.parseDouble(jsonObject.get("grandTotalRaisedExcludingGiftAid").getAsString());
            double target = Double.parseDouble(jsonObject.get("fundraisingTarget").getAsString());
            if (total > settings.getLastTotal())
            {
                settings.setLastTotal(total);
                settings.setLastTarget(target);
                saveSettings();
                updateTotalRaisedMessage();
                sendMessageToAll();
                sendMessageToDiscord();
                sendMessageToFacebook();
                musicHandler.loadAndPlayPriority("justgiving.mp3");
            } else if (totalRaisedMessage == null || target != settings.getLastTarget())
            {
                settings.setLastTarget(target);
                saveSettings();
                updateTotalRaisedMessage();
            }
        }
    }

    private void sendMessageToAll()
    {
        if (totalRaisedMessage != null && settings.getChannels() != null)
        {
            for (String channel : settings.getChannels())
                twitchBot.sendMessage(channel, totalRaisedMessage);
        }
    }

    private void sendMessageToDiscord()
    {
        if (totalRaisedMessage != null && settings.getDiscordChannelIds() != null)
        {
            for (long channel : settings.getDiscordChannelIds())
                discordBot.sendMessage(channel, totalRaisedMessage);
        }
    }

    private void sendMessageToFacebook()
    {
        try
        {
            if (totalRaisedMessage != null && settings.getFacebookWebhook() != null)
            {
                JsonObject json = new JsonObject();
                json.addProperty("message", totalRaisedMessage);
                json.addProperty("link", "https://www.justgiving.com/fundraising/" + settings.getCampaignName());
                HttpHandler.postUTF8Request(new URL(settings.getFacebookWebhook()), json.toString());
            }
        } catch (Exception e)
        {
            log.error("Error with facebook post", e);
        }
    }

    public Mono<Void> amountRaised(CommandEvent<?> event)
    {
        if (totalRaisedMessage != null)
        {
            if (event instanceof TwitchCommandEvent)
                return ((TwitchCommandEvent) event).multiRespond(liveStreamManager, totalRaisedMessage);
            else
                return event.respond(totalRaisedMessage);
        }
        return event.empty();
    }

    private void updateTotalRaisedMessage()
    {
        JsonElement jsonTree = JsonParser.parseString(data);
        JsonObject jsonObject = jsonTree.getAsJsonObject();
        String total = toCurrency(jsonObject.get("grandTotalRaisedExcludingGiftAid").getAsString());
        String target = toCurrency(jsonObject.get("fundraisingTarget").getAsString());
        String percentage = jsonObject.get("totalRaisedPercentageOfFundraisingTarget").getAsString() + "%";
        String message = settings.getMessage();
        message = message.replace("$total", total);
        message = message.replace("$target", target);
        message = message.replace("$percentage", percentage);
        totalRaisedMessage = message;
    }

    private String toCurrency(String amount)
    {
        String[] split = amount.split("\\.");
        if (split.length < 2)
            return "£" + split[0] + ".00";
        else if (split[1].length() < 2)
            return "£" + split[0] + "." + split[1] + "0";
        else
            return "£" + amount;
    }

    public JustGivingSettings getSettings()
    {
        return settings;
    }
}