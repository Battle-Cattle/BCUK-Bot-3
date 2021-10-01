package com.expiredminotaur.bcukbot.discord.command;

import com.expiredminotaur.bcukbot.command.CommandEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.legacy.LegacyEmbedCreateSpec;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

public class DiscordCommandEvent extends CommandEvent<MessageCreateEvent>
{

    public DiscordCommandEvent(MessageCreateEvent event)
    {
        super(event);
    }

    @Override
    public String getOriginalMessage()
    {
        return event.getMessage().getContent();
    }

    @Override
    public Mono<Void> respond(String message)
    {
        return event.getMessage().getChannel().flatMap(c -> c.createMessage(message)).then();
    }

    public Mono<Message> respond(Consumer<LegacyEmbedCreateSpec> embed)
    {
        return event.getMessage().getChannel().flatMap(c -> c.createMessage(m -> m.setEmbed(embed)));
    }

    @Override
    public String getSourceName()
    {
        return "Discord";
    }
}
