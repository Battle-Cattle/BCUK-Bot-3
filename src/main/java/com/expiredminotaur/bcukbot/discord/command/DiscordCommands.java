package com.expiredminotaur.bcukbot.discord.command;

import com.expiredminotaur.bcukbot.discord.command.category.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.TreeMap;

@Component
public class DiscordCommands
{
    private final TreeMap<String, Category> commands = new TreeMap<>();

    @Autowired
    public final void registerCommands(List<Category> categories)
    {
        for (Category category : categories)
            commands.put(category.getName(), category);
    }

    public Mono<Void> processCommand(DiscordCommandEvent event)
    {
        return Flux.fromIterable(commands.values()).map(c -> c.processCommand(event).subscribe()).then();
    }
}
