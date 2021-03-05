package com.expiredminotaur.bcukbot.justgiving;

import com.expiredminotaur.bcukbot.command.CommandEvent;
import com.expiredminotaur.bcukbot.discord.DiscordBot;
import com.expiredminotaur.bcukbot.discord.music.MusicHandler;
import com.expiredminotaur.bcukbot.twitch.TwitchBot;
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
    private final JustGivingSettings settings;
    private String data;
    private String totalRaisedMessage = null;
    private ScheduledFuture<?> task = null;

    public JustGivingAPI(@Autowired TwitchBot twitchBot, @Autowired DiscordBot discordBot, @Autowired @Lazy MusicHandler musicHandler)
    {
        this.twitchBot = twitchBot;
        this.discordBot = discordBot;
        this.musicHandler = musicHandler;
        JustGivingSettings settings;
        try (FileReader fr = new FileReader(JustGivingSettings.fileName))
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
        try (FileWriter fw = new FileWriter(JustGivingSettings.fileName))
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
        if (settings.autoCheckEnabled && task == null)
        {
            task = scheduler.scheduleAtFixedRate(this::checkForNewData, 0, 1, TimeUnit.SECONDS);
        } else if (!settings.autoCheckEnabled && task != null)
        {
            task.cancel(false);
            task = null;
        }
    }

    private BufferedReader request(URL url) throws Exception
    {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        switch (conn.getResponseCode())
        {
            case 200:
                return new BufferedReader(new InputStreamReader((conn.getInputStream())));
            case 304:
                //no change
                return null;
            default:
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode() + " From:" + url.toString());
        }
    }

    private void post(URL url, String inputJson) throws Exception
    {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        try(OutputStream os = conn.getOutputStream()) {
            byte[] input = inputJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        if (conn.getResponseCode() == 200)
        {
            return;
        }
        throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode() + " From:" + url.toString());
    }

    private void checkForNewData()
    {
        try
        {
            URL url = new URL(String.format("https://api.justgiving.com/%s/v1/fundraising/pages/%s", settings.appId, settings.campaignName));
            BufferedReader br = request(url);
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
            if (total > settings.lastTotal)
            {
                settings.lastTotal = total;
                settings.lastTarget =target;
                saveSettings();
                updateTotalRaisedMessage();
                sendMessageToAll();
                sendMessageToDiscord();
                sendMessageToFacebook();
                musicHandler.loadAndPlayPriority("justgiving.mp3");
            } else if (totalRaisedMessage == null || target != settings.lastTarget)
            {
                settings.lastTarget = target;
                updateTotalRaisedMessage();
            }
        }
    }

    private void sendMessageToAll()
    {
        if (totalRaisedMessage != null && settings.channels != null)
        {
            for (String channel : settings.channels)
                twitchBot.sendMessage(channel, totalRaisedMessage);
        }
    }

    private void sendMessageToDiscord()
    {
        if (totalRaisedMessage != null && settings.discordChannelId != -1L)
        {
            discordBot.sendMessage(settings.discordChannelId, totalRaisedMessage);
        }
    }

    private void sendMessageToFacebook()
    {
        try
        {
            if(totalRaisedMessage != null && settings.facebookWebhook != null)
            {
                JsonObject json = new JsonObject();
                json.addProperty("message", totalRaisedMessage);
                json.addProperty("link", "https://www.justgiving.com/fundraising/" + settings.campaignName);
                post(new URL(settings.facebookWebhook), json.toString());
            }
        } catch (Exception e)
        {
            log.error("Error with facebook post", e);
        }
    }

    public <R> R amountRaised(CommandEvent<?, R> event)
    {
        if (totalRaisedMessage != null)
        {
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
        String message = settings.message;
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