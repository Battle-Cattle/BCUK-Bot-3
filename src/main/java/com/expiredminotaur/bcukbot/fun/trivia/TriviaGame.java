package com.expiredminotaur.bcukbot.fun.trivia;

import com.expiredminotaur.bcukbot.discord.Emoji;
import com.expiredminotaur.bcukbot.discord.command.DiscordCommandEvent;
import com.expiredminotaur.bcukbot.fun.trivia.api.Questions;
import com.expiredminotaur.bcukbot.fun.trivia.api.TriviaAPI;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class TriviaGame
{
    @Autowired
    private TriviaAPI triviaAPI;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public Mono<Void> trivia(DiscordCommandEvent event)
    {
        MessageChannel channel = event.getEvent().getMessage().getChannel().block();
        if (channel != null)
        {
            try
            {
                Questions.Question question = triviaAPI.getQuestion();
                EmbedCreateSpec embed = setupEmbed(question);

                postQuestion(embed, question, channel);

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return Mono.empty().then();
    }

    private EmbedCreateSpec setupEmbed(Questions.Question question)
    {
        EmbedCreateSpec spec = EmbedCreateSpec.create().withTitle(parseText(question.getQuestion()));
        DifficultyColour difficultyColour = DifficultyColour.fromString(question.getDifficulty());
        if (difficultyColour != null)
            spec = spec.withColor(difficultyColour.color);
        return spec;
    }

    private void postQuestion(EmbedCreateSpec embed, Questions.Question question, MessageChannel channel) throws IllegalArgumentException
    {
        switch (question.getType())
        {
            case "multiple":
                multiQuestion(embed, question, channel);
                break;
            case "boolean":
                boolQuestion(embed, question, channel);
                break;
            default:
                throw new IllegalArgumentException(question.getType());
        }
    }

    private void multiQuestion(EmbedCreateSpec embed, Questions.Question question, MessageChannel channel)
    {
        List<String> answers = question.getIncorrectAnswers();
        answers.add(question.getCorrectAnswer());
        Collections.shuffle(answers);
        String s = Emoji.A.getRaw() + parseText(answers.get(0)) + "\n" +
                Emoji.B.getRaw() + parseText(answers.get(1)) + "\n" +
                Emoji.C.getRaw() + parseText(answers.get(2)) + "\n" +
                Emoji.D.getRaw() + parseText(answers.get(3));
        EmbedCreateSpec finalEmbed = embed.withDescription(s);
        Message message = channel.createMessage(MessageCreateSpec.create().withEmbeds(finalEmbed)).block();
        if (message != null)
        {
            scheduler.schedule(() -> multiReactions(message), 1L, TimeUnit.SECONDS);
            scheduler.schedule(() -> multiAnswer(channel, message, question, answers), 60L, TimeUnit.SECONDS);
        }
    }

    private void boolQuestion(EmbedCreateSpec embed, Questions.Question question, MessageChannel channel)
    {
        String s = Emoji.TRUE.getRaw() + "True" + "\n" +
                Emoji.FALSE.getRaw() + "False";
        EmbedCreateSpec finalEmbed = embed.withDescription(s);
        Message message = channel.createMessage(MessageCreateSpec.create().withEmbeds(finalEmbed)).block();
        if (message != null)
        {
            scheduler.schedule(() -> boolReactions(message), 1L, TimeUnit.SECONDS);
            scheduler.schedule(() -> boolAnswer(channel, message, question), 60L, TimeUnit.SECONDS);
        }
    }

    private void multiReactions(Message message)
    {
        message.addReaction(Emoji.A).block();
        message.addReaction(Emoji.B).block();
        message.addReaction(Emoji.C).block();
        message.addReaction(Emoji.D).block();
    }

    private void boolReactions(Message message)
    {
        message.addReaction(Emoji.TRUE).block();
        message.addReaction(Emoji.FALSE).block();
    }

    private void multiAnswer(MessageChannel channel, Message message, Questions.Question question, List<String> answers)
    {
        int correctId = answers.indexOf(question.getCorrectAnswer());
        List<Integer> wrongId = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        wrongId.remove(correctId);
        ReactionEmoji.Unicode[] reactions = new ReactionEmoji.Unicode[]{Emoji.A, Emoji.B, Emoji.C, Emoji.D};
        List<User> correct = new ArrayList<>();
        List<User> correctList = message.getReactors(reactions[correctId]).collectList().block();
        if (correctList != null)
            correct.addAll(correctList);
        for (int id : wrongId)
        {
            List<User> wrongList = message.getReactors(reactions[id]).collectList().block();
            if (wrongList != null)
                correct.removeAll(wrongList);
        }
        message.delete().subscribe();
        correctAnswerPost(channel, question, reactions[correctId], answers.get(correctId), correct);
    }

    private void boolAnswer(MessageChannel channel, Message message, Questions.Question question)
    {

        List<User> correct = new ArrayList<>();
        ReactionEmoji.Unicode correctEmoji;
        ReactionEmoji.Unicode incorrectEmoji;
        boolean answer = question.getCorrectAnswer().equals("True");
        correctEmoji = answer ? Emoji.TRUE : Emoji.FALSE;
        incorrectEmoji = answer ? Emoji.FALSE : Emoji.TRUE;

        List<User> correctList = message.getReactors(correctEmoji).collectList().block();
        if (correctList != null)
            correct.addAll(correctList);
        List<User> wrongList = message.getReactors(incorrectEmoji).collectList().block();
        if (wrongList != null)
            correct.removeAll(wrongList);

        message.delete().subscribe();
        correctAnswerPost(channel, question, correctEmoji, question.getCorrectAnswer(), correct);
    }

    private void correctAnswerPost(MessageChannel channel, Questions.Question question, ReactionEmoji.Unicode emoji, String answer, List<User> correct)
    {
        EmbedCreateSpec embed = EmbedCreateSpec.create().withTitle(parseText(question.getQuestion()));
        {
            DifficultyColour difficultyColour = DifficultyColour.fromString(question.getDifficulty());
            if (difficultyColour != null)
                embed = embed.withColor(difficultyColour.color);

            StringBuilder s = new StringBuilder();
            s.append(emoji.getRaw()).append(parseText(answer));
            embed = embed.withDescription(s.toString());
            if (correct.size() > 0)
            {
                s = new StringBuilder();
                s.append("Correct: ");
                //TODO: we should convert to members and get display names here
                s.append(correct.stream().map(User::getUsername).collect(Collectors.joining(", ")));
                embed = embed.withFooter(EmbedCreateFields.Footer.of(s.toString(), ""));
            }
        }
        channel.createMessage(MessageCreateSpec.create().withEmbeds(embed)).block();
    }


    private String parseText(String text)
    {
        text = text.replace("&quot;", "\"");
        text = text.replace("&#039;", "'");
        text = text.replace("*", "\\*");
        text = text.replace("_", "\\_");
        text = text.replace("~", "\\~");
        text = text.replace("&amp;", "&");

        return text;
    }
}
