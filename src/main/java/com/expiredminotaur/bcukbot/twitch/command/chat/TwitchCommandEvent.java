package com.expiredminotaur.bcukbot.twitch.command.chat;


import com.expiredminotaur.bcukbot.command.CommandEvent;
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
        event.getTwitchChat().sendMessage(event.getChannel().getName(), message);
        return empty();
    }

    @Override
    public String getSourceName()
    {
        return "Twitch_" + event.getChannel().getName();
    }
}
