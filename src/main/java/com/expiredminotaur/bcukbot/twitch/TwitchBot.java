package com.expiredminotaur.bcukbot.twitch;

import com.expiredminotaur.bcukbot.BotService;
import com.expiredminotaur.bcukbot.HttpHandler;
import com.expiredminotaur.bcukbot.discord.music.SFXHandler;
import com.expiredminotaur.bcukbot.fun.counters.CounterHandler;
import com.expiredminotaur.bcukbot.sql.command.custom.CommandRepository;
import com.expiredminotaur.bcukbot.sql.command.custom.CustomCommand;
import com.expiredminotaur.bcukbot.sql.user.User;
import com.expiredminotaur.bcukbot.sql.user.UserRepository;
import com.expiredminotaur.bcukbot.twitch.command.chat.TwitchCommandEvent;
import com.expiredminotaur.bcukbot.twitch.command.chat.TwitchCommands;
import com.expiredminotaur.bcukbot.twitch.command.whisper.WhisperCommandEvent;
import com.expiredminotaur.bcukbot.twitch.command.whisper.WhisperCommands;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.user.PrivateMessageEvent;
import com.github.twitch4j.helix.domain.Stream;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Component
public class TwitchBot implements BotService
{
    private final Logger log = LoggerFactory.getLogger(TwitchBot.class);
    @Autowired
    private TwitchCommands twitchCommands;
    @Autowired
    private WhisperCommands whisperCommands;
    @Autowired
    private SFXHandler sfxHandler;
    @Autowired
    private BanHandler banHandler;
    @Autowired
    private CommandRepository customCommands;
    @Autowired
    private CounterHandler counterHandler;
    private final UserRepository userRepository;
    private TwitchClient twitchClient;
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private String accessToken;

    public TwitchBot(@Autowired UserRepository userRepository)
    {
        this.userRepository = userRepository;
        start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    private static String getAccessToken() throws Exception
    {
        String url = "https://id.twitch.tv/oauth2/token?" +
                "client_id=" + URLEncoder.encode(System.getenv("BCUK_BOT_TWITCH_CLIENT_ID"), "UTF-8") +
                "&client_secret=" + URLEncoder.encode(System.getenv("BCUK_BOT_TWITCH_CLIENT_SECRET"), "UTF-8") +
                "&grant_type=client_credentials";
        JsonElement json = JsonParser.parseReader(HttpHandler.postRequest(new URL(url)));
        if (json.isJsonObject())
        {
            return ((JsonObject) json).get("access_token").getAsString();
        } else
            throw new RuntimeException(("Error reading access token"));
    }

    @Override
    public void start()
    {
        try
        {
            accessToken = getAccessToken();
        } catch (Exception e)
        {
            log.error("Failed to get access Token", e);
            return;
        }
        setupThreads();
        TwitchClientBuilder clientBuilder = TwitchClientBuilder.builder();
        OAuth2Credential chatOAuth = new OAuth2Credential("twitch", System.getenv("BCUK_BOT_TWITCH_CHAT_OAUTH"));
        OAuth2Credential appOAuth = new OAuth2Credential("twitch", accessToken);
        twitchClient = clientBuilder
                .withClientId(System.getenv("BCUK_BOT_TWITCH_CLIENT_ID"))
                .withClientSecret(System.getenv("BCUK_BOT_TWITCH_CLIENT_SECRET"))
                .withEnableHelix(true)
                .withChatAccount(chatOAuth)
                .withEnableChat(true)
                .withScheduledThreadPoolExecutor(scheduledThreadPoolExecutor)
                .withDefaultAuthToken(appOAuth)
                .build();
        setupEvents();
        joinChannels();
    }

    @Override
    public void stop()
    {
        twitchClient.close();
        scheduledThreadPoolExecutor.shutdown();
        twitchClient = null;
        scheduledThreadPoolExecutor = null;
    }

    @Override
    public boolean isRunning()
    {
        return twitchClient != null;
    }

    private void setupThreads()
    {
        BasicThreadFactory threadFactory = new BasicThreadFactory.Builder()
                .namingPattern("twitch_chat-%d")
                .daemon(false)
                .priority(Thread.NORM_PRIORITY)
                .build();

        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
        scheduledThreadPoolExecutor.setThreadFactory(threadFactory);
        scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
        scheduledThreadPoolExecutor.setMaximumPoolSize(Runtime.getRuntime().availableProcessors() * 8);
    }

    private void setupEvents()
    {
        SimpleEventHandler handler = twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class);
        handler.onEvent(ChannelMessageEvent.class, this::onChannelMessage);
        handler.onEvent(PrivateMessageEvent.class, this::onWhisper);
    }

    private void onChannelMessage(ChannelMessageEvent event)
    {
        TwitchCommandEvent cEvent = new TwitchCommandEvent(event);
        if (!banHandler.checkBannedPhrases(cEvent))
        {
            String command = event.getMessage().split(" ", 2)[0];
            sfxHandler.play(command);
            twitchCommands.processCommand(cEvent);
            counterHandler.processCommand(cEvent);
            CustomCommand custom = customCommands.findTwitch(event.getChannel().getName().toLowerCase(), command.toLowerCase());
            if (custom != null)
                event.getTwitchChat().sendMessage(event.getChannel().getName(), custom.getOutput());
        }
    }

    private void onWhisper(PrivateMessageEvent event)
    {
        whisperCommands.processCommand(new WhisperCommandEvent(event, twitchClient));
    }

    public void joinChannels()
    {
        for (User user : userRepository.findByIsTwitchBotEnabledIsTrue())
        {
            String name = user.getTwitchName();
            if (name != null && !name.equals(""))
            {
                twitchClient.getChat().joinChannel(name);
            }
        }

    }

    public void sendMessage(String channel, String message)
    {
        twitchClient.getChat().sendMessage(channel, message);
    }

    public List<Stream> getStreams(List<String> channels)
    {
        return twitchClient.getHelix().getStreams(accessToken, "", null, 1, null, null, null, channels).execute().getStreams();
    }
}
