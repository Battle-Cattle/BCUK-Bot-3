package com.expiredminotaur.bcukbot.twitch.streams;

import com.expiredminotaur.bcukbot.discord.DiscordBot;
import com.expiredminotaur.bcukbot.sql.twitch.streams.group.Group;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MultiTwitchHandler
{
    private final DiscordBot discordBot;
    private final Map<String, MultiTwitch> multiTwitchs = new HashMap<>();

    public MultiTwitchHandler(DiscordBot discordBot)
    {
        this.discordBot = discordBot;
    }

    public void update(Map<String, StreamData> groupData, Group group)
    {
        Map<String, Set<String>> currentStreams = setupMap(groupData);

        for (String game : currentStreams.keySet())
        {
            processGame(game, group, currentStreams);
        }
    }

    private Map<String, Set<String>> setupMap(Map<String, StreamData> groupData)
    {
        Map<String, Set<String>> currentStreams = new HashMap<>();
        for (Map.Entry<String, StreamData> user : groupData.entrySet())
        {
            currentStreams.computeIfAbsent(user.getValue().getGame(), k -> new HashSet<>()).add(user.getKey());
        }
        for(String game: multiTwitchs.keySet())
        {
            currentStreams.computeIfAbsent(game, k -> new HashSet<>());
        }
        return currentStreams;
    }

    private void processGame(String game, Group group, Map<String, Set<String>> currentStreams)
    {
        if (multiTwitchs.containsKey(game))
        {
            updateGame(game, group, currentStreams);
        } else
        {
            if (currentStreams.get(game).size() > 1)
            {
                MultiTwitch multiTwitch = new MultiTwitch(currentStreams.get(game));
                formatAndSendMulti(multiTwitch, group, game);
                multiTwitchs.put(game, multiTwitch);
            }
        }
    }

    private void updateGame(String game, Group group, Map<String, Set<String>> currentStreams)
    {
        MultiTwitch multiTwitch = multiTwitchs.get(game);
        if (!currentStreams.get(game).equals(multiTwitch.getUsers()))
        {
            deleteOldMessage(group, multiTwitch);
            if (currentStreams.get(game).size() > 1)
            {
                multiTwitch.setUsers(currentStreams.get(game));
                formatAndSendMulti(multiTwitch, group, game);

            } else
            {
                multiTwitchs.remove(game);
            }
        }
    }

    private void deleteOldMessage(Group group, MultiTwitch multiTwitch)
    {
        if (group.isDeleteOldPosts())
        {
            multiTwitch.deleteMessage();
        }
    }

    private void formatAndSendMulti(MultiTwitch mt, Group group, String game)
    {
        String message = group.getMultiTwitchMessage();
        message = message.replace("%game%", game);
        message = message.replace("%link%", mt.getLink());
        mt.setMessage(discordBot.sendAndGetMessage(group.getDiscordChannel(), message));
    }

    public MultiTwitch getMultiTwitch(String channel)
    {
        for (MultiTwitch mt : multiTwitchs.values())
        {
            if (mt.getUsers().contains(channel))
                return mt;
        }
        return null;
    }
}
