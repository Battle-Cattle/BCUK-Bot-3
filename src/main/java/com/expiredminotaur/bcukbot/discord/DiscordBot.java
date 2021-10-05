package com.expiredminotaur.bcukbot.discord;

import com.expiredminotaur.bcukbot.BotService;
import com.expiredminotaur.bcukbot.discord.command.DiscordCommandEvent;
import com.expiredminotaur.bcukbot.discord.command.DiscordCommands;
import com.expiredminotaur.bcukbot.discord.music.SFXHandler;
import com.expiredminotaur.bcukbot.fun.counters.CounterHandler;
import com.expiredminotaur.bcukbot.sql.command.custom.CommandRepository;
import com.expiredminotaur.bcukbot.sql.command.custom.CustomCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.gateway.intent.IntentSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class DiscordBot implements BotService
{
    private Thread thread;
    private BotThread botThread;
    @Autowired
    DiscordCommands commands;
    @Autowired
    CommandRepository customCommands;
    @Autowired
    private SFXHandler sfxHandler;
    @Autowired
    private PointsSystem pointsSystem;
    @Autowired
    private CounterHandler counterHandler;

    public DiscordBot()
    {
        start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    @Override
    public void start()
    {
        if (botThread == null)
        {
            botThread = new BotThread();
            thread = new Thread(botThread);
            thread.setName("DiscordBot");
            thread.start();
        }
    }

    @Override
    public void stop()
    {
        if (botThread != null && botThread.gateway != null)
        {
            botThread.gateway.logout().block();
        }
        botThread = null;
        thread = null;
    }

    @Override
    public boolean isRunning()
    {
        return botThread != null;
    }

    public DiscordClient getClient()
    {
        return botThread.client;
    }

    public GatewayDiscordClient getGateway()
    {
        return botThread.gateway;
    }

    public void sendMessage(Long channelID, String message)
    {
        botThread.gateway.getChannelById(Snowflake.of(channelID))
                .cast(MessageChannel.class).flatMap(c -> c.createMessage(message)).subscribe();
    }

    public Message sendAndGetMessage(Long channelID, String message)
    {
        return botThread.gateway.getChannelById(Snowflake.of(channelID))
                .cast(MessageChannel.class).flatMap(c -> c.createMessage(message)).block();
    }

    public Mono<Channel> getChannel(Long channelId)
    {
        return botThread.gateway.getChannelById(Snowflake.of(channelId));
    }

    public class BotThread implements Runnable
    {
        private DiscordClient client;
        private GatewayDiscordClient gateway;

        @Override
        public void run()
        {
            String token = System.getenv("BCUK_BOT_DISCORD_TOKEN");
            client = DiscordClient.create(token);
            gateway = client.gateway().setEnabledIntents(IntentSet.all()).login().block();
            if (gateway == null)
                stop();
            else
            {
                gateway.on(MessageCreateEvent.class).subscribe(this::onMessage);
                gateway.onDisconnect().block();
            }
        }

        private void onMessage(MessageCreateEvent event)
        {
            Optional<User> author = event.getMessage().getAuthor();
            if (author.isPresent() && !author.get().isBot() && event.getGuildId().isPresent())
            {
                String command = event.getMessage().getContent().split(" ", 2)[0];
                sfxHandler.play(command);
                DiscordCommandEvent cEvent = new DiscordCommandEvent(event);
                commands.processCommand(cEvent).subscribe();
                counterHandler.processCommand(cEvent).subscribe();
                CustomCommand custom = customCommands.findDiscord(command.toLowerCase());
                if (custom != null)
                    event.getMessage().getChannel().flatMap(c -> c.createMessage(custom.getOutput())).subscribe();
                event.getMember().ifPresent(m -> pointsSystem.addXP(m));
            }
        }
    }
}
