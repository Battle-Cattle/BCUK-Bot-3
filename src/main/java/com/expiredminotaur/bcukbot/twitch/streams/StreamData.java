package com.expiredminotaur.bcukbot.twitch.streams;

import com.expiredminotaur.bcukbot.discord.DiscordBot;
import com.expiredminotaur.bcukbot.sql.twitch.streams.group.Group;
import com.github.twitch4j.helix.domain.Stream;
import discord4j.core.object.entity.Message;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class StreamData
{
    private final DiscordBot discordBot;
    private Stream stream;
    private long lastUpdated;
    private Message discordMessage;

    public StreamData(DiscordBot discordBot)
    {
        this.discordBot = discordBot;
        lastUpdated = System.currentTimeMillis();
    }

    public void update(Group group, Stream newStream)
    {
        if (stream == null)
        {
            stream = newStream;
            discordMessage = discordBot.sendAndGetMessage(group.getDiscordChannel(), getLiveMessage(group));
        } else
        {
            if (!Objects.equals(stream.getGameId(), newStream.getGameId()))
            {
                stream = newStream;
                if (group.isDeleteOldPosts() && discordMessage != null)
                {
                    discordMessage.delete("Old Stream Message").subscribe();
                }
                discordMessage = discordBot.sendAndGetMessage(group.getDiscordChannel(), getNewGameMessage(group));
            }
        }
        lastUpdated = System.currentTimeMillis();
    }

    public boolean checkValid(Group group)
    {
        if (System.currentTimeMillis() - lastUpdated > TimeUnit.MINUTES.toMillis(5))
        {
            if (group.isDeleteOldPosts() && discordMessage != null)
            {
                discordMessage.delete("Old Stream Message").subscribe();
            }
            return false;
        }
        return true;
    }

    private String getLiveMessage(Group group)
    {
        String message = group.getLiveMessage();
        return replacePlaceholders(message);
    }

    private String getNewGameMessage(Group group)
    {
        String message = group.getNewGameMessage();
        return replacePlaceholders(message);
    }

    private String replacePlaceholders(String message)
    {
        String name = stream.getUserName().replace("_", "\\_");
        message = message.replace("%channel%", name);
        message = message.replace("%game%", stream.getGameName());
        message = message.replace("%link%", String.format("https://www.twitch.tv/%s", stream.getUserName().toLowerCase()));
        return message;
    }

    public String getGame()
    {
        String game = stream.getGameName();
        if (game == null)
            return "something";
        return stream.getGameName();
    }

    @Deprecated
    /*
    DEBUG CODE TO BE REMOVED LATER
     */
    public long debugLastUpdated()
    {
        return lastUpdated;
    }
}
