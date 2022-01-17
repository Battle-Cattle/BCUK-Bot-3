package com.expiredminotaur.bcukbot.twitch.command.chat;


import com.expiredminotaur.bcukbot.command.CommandEvent;
import com.expiredminotaur.bcukbot.twitch.streams.LiveStreamManager;
import com.expiredminotaur.bcukbot.twitch.streams.MultiTwitch;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import reactor.core.publisher.Mono;

public class TwitchCommandEvent extends CommandEvent<ChannelMessageEvent>
{

    public TwitchCommandEvent(ChannelMessageEvent event)
    {
        super(event);
    }

    @Override
    public String getOriginalMessage()
    {
        return event.getMessage();
    }

    public Mono<Void> respond(String message)
    {
        event.getTwitchChat().sendMessage(event.getChannel().getName(), message, null, event.getMessageEvent().getMessageId().orElse(null));
        return empty();
    }

    public Mono<Void> multiRespond(LiveStreamManager liveStreamManager, String message)
    {
        MultiTwitch mt = liveStreamManager.getMultiTwitch(event.getChannel().getName());
        if (mt != null)
        {
            mt.sendToAllUsers(event.getTwitchChat(), message);
        } else
            event.getTwitchChat().sendMessage(event.getChannel().getName(), message, null, event.getMessageEvent().getMessageId().orElse(null));
        return empty();
    }

    @Override
    public String getSourceName()
    {
        return "Twitch_" + event.getChannel().getName();
    }
}
