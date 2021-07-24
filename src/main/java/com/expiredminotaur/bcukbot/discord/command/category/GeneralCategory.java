package com.expiredminotaur.bcukbot.discord.command.category;

import com.expiredminotaur.bcukbot.discord.command.DiscordCommand;
import com.expiredminotaur.bcukbot.discord.command.DiscordCommandEvent;
import com.expiredminotaur.bcukbot.discord.command.DiscordPermissions;
import com.expiredminotaur.bcukbot.fun.dadjokes.JokeAPI;
import com.expiredminotaur.bcukbot.justgiving.JustGivingAPI;
import com.expiredminotaur.bcukbot.sql.collection.clip.ClipUtils;
import com.expiredminotaur.bcukbot.sql.collection.joke.JokeUtils;
import com.expiredminotaur.bcukbot.sql.collection.quote.QuoteUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
public class GeneralCategory extends Category
{
    private QuoteUtils quoteUtils;
    private ClipUtils clipUtils;
    private JokeUtils jokeUtils;
    private JustGivingAPI justGivingAPI;
    private List<Category> categories;

    public GeneralCategory()
    {
        commands.put("!Commands", new DiscordCommand(this::commands, DiscordPermissions::general));
        commands.put("!Quote", new DiscordCommand(e -> quoteUtils.processCommand(e), DiscordPermissions::general));
        commands.put("!Clip", new DiscordCommand(e -> clipUtils.processCommand(e), DiscordPermissions::general));
        commands.put("!Joke", new DiscordCommand(e -> jokeUtils.processCommand(e), DiscordPermissions::general));
        commands.put("!TotalRaised", new DiscordCommand(e -> justGivingAPI.amountRaised(e), DiscordPermissions::general));
        commands.put("!DadJoke", new DiscordCommand(JokeAPI::jokeCommand, DiscordPermissions::general));
        commands.put("!Sfx", new DiscordCommand(e -> e.respond(sfxList()), DiscordPermissions::general));
        commands.put("!SfxC", new DiscordCommand(e -> e.respond(sfxCategoriesList()), DiscordPermissions::general));
    }

    private Mono<Void> commands(DiscordCommandEvent event)
    {
        StringBuilder s = new StringBuilder();
        s.append("```\nAvailable commands:\n");
        for (Category category : categories)
        {
            s.append(String.format("\n%s\n", category.getName()));
            s.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

            for (String key : category.getCommandList(event))
            {
                s.append(String.format("%s\n", key));
            }
        }
        s.append("```");
        return event.getEvent().getMessage().getChannel().flatMap(channel -> channel.createMessage(s.toString())).then();
    }

    @Autowired
    public final void setCategories(List<Category> categories)
    {
        this.categories = new ArrayList<>();
        this.categories.add(this);
        this.categories.addAll(categories);
    }

    @Autowired
    public final void setQuoteUtils(QuoteUtils quoteUtils)
    {
        this.quoteUtils = quoteUtils;
    }

    @Autowired
    public final void setClipUtils(ClipUtils clipUtils)
    {
        this.clipUtils = clipUtils;
    }

    @Autowired
    public final void setJokeUtils(JokeUtils jokeUtils)
    {
        this.jokeUtils = jokeUtils;
    }

    @Autowired
    @Lazy
    public final void setJustGivingAPI(JustGivingAPI justGivingAPI)
    {
        this.justGivingAPI = justGivingAPI;
    }

    @Override
    public String getName()
    {
        return "GENERAL";
    }
}
