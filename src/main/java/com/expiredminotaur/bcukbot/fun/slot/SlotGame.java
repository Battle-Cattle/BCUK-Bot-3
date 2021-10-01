package com.expiredminotaur.bcukbot.fun.slot;

import com.expiredminotaur.bcukbot.discord.command.DiscordCommandEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.legacy.LegacyEmbedCreateSpec;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class SlotGame
{
    private static final Random rng = new Random();
    private static final String[] emojis = {"\uD83C\uDF47", "\uD83C\uDF4A", "\uD83C\uDF52", "\uD83C\uDF53"};
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final long delay = 1L;

    public Mono<Void> startGame(DiscordCommandEvent event)
    {
        Outcome outcome = getOutcome();
        String[] display = new String[outcome.result.length];
        Arrays.fill(display, "\u2753");
        Consumer<LegacyEmbedCreateSpec> embed = spec -> spec.setTitle("Slot");
        Consumer<LegacyEmbedCreateSpec> embedWithResult = embed.andThen(spec ->
                spec.addField("Outcome", String.join(" ", display), false)
        );
        Message message = event.respond(embedWithResult).block();
        if (message != null)
        {
            scheduler.schedule(() -> update(message, embed, outcome, display, 0), delay, TimeUnit.SECONDS);
        }
        return Mono.empty().then();
    }

    private void update(Message message, Consumer<LegacyEmbedCreateSpec> embed, Outcome outcome, String[] display, int i)
    {
        display[i] = outcome.result[i];
        Consumer<LegacyEmbedCreateSpec> embedWithResult = embed.andThen(spec ->
                spec.addField("Outcome", String.join(" ", display), false)
        );

        message.edit(messageEditSpec -> messageEditSpec.setEmbed(embedWithResult)).block();
        if (++i < outcome.result.length)
        {
            int newIndex = i;
            scheduler.schedule(() -> update(message, embed, outcome, display, newIndex), delay, TimeUnit.SECONDS);
        } else
        {
            scheduler.schedule(() -> showResult(message, embedWithResult, outcome, display), delay, TimeUnit.SECONDS);

        }
    }

    private void showResult(Message message, Consumer<LegacyEmbedCreateSpec> embed, Outcome outcome, String[] display)
    {
        Consumer<LegacyEmbedCreateSpec> embedWithResult = embed.andThen(spec ->
                spec.addField("Result", (outcome.win ? "\u2705" : "\u274c"), false)
        );
        message.edit(messageEditSpec -> messageEditSpec.setEmbed(embedWithResult)).block();
    }

    public Outcome getOutcome()
    {
        return new Outcome();
    }

    public static class Outcome
    {
        public final String[] result;
        public final boolean win;

        Outcome()
        {
            result = new String[]{random(), random(), random()};
            win = result[0].equals(result[1]) && result[1].equals(result[2]);
        }

        private String random()
        {
            return emojis[rng.nextInt(emojis.length)];
        }
    }
}