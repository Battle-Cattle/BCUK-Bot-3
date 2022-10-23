package com.expiredminotaur.bcukbot.twitch.streams;

import com.expiredminotaur.bcukbot.discord.DiscordBot;
import com.expiredminotaur.bcukbot.sql.twitch.streams.group.Group;
import com.expiredminotaur.bcukbot.sql.twitch.streams.group.GroupRepository;
import com.expiredminotaur.bcukbot.sql.twitch.streams.streamer.Streamer;
import com.expiredminotaur.bcukbot.twitch.TwitchBot;
import com.expiredminotaur.bcukbot.twitch.command.chat.TwitchCommandEvent;
import com.github.twitch4j.helix.domain.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LiveStreamManager
{
    @Lazy
    @Autowired
    private TwitchBot twitchBot;
    @Lazy
    @Autowired
    private DiscordBot discordBot;
    @Autowired
    private GroupRepository groupRepository;

    private final Map<String, Map<String, StreamData>> streams = new HashMap<>();
    private final Map<String, MultiTwitchHandler> multiTwitchHandlers = new HashMap<>();

    @Scheduled(cron = "*/15 * * * * *")//every 15th second
    private void getStreams()
    {
        List<Group> groups = groupRepository.findAll();
        groups.forEach(group ->
        {
            Map<String, StreamData> groupData = streams.computeIfAbsent(group.getName(), k -> new HashMap<>());
            Set<Streamer> streamers = group.getStreamers();
            if (streamers.size() > 0)
            {
                List<String> streamerNames = streamers.stream().map(Streamer::getName).collect(Collectors.toList());
                List<Stream> streams = twitchBot.getStreams(streamerNames);
                streams.forEach(s ->
                {
                    StreamData streamData = groupData.computeIfAbsent(s.getUserName().toLowerCase(), n -> new StreamData(discordBot));
                    streamData.update(group, s);
                });
            }
            groupData.entrySet().removeIf(s -> !s.getValue().checkValid(group));
            if (group.isMultiTwitch())
            {
                multiTwitchHandlers.computeIfAbsent(group.getName(), k -> new MultiTwitchHandler(discordBot)).update(groupData, group);
            }
        });
    }

    public MultiTwitch getMultiTwitch(String channelName)
    {
        for (MultiTwitchHandler mth : multiTwitchHandlers.values())
        {
            MultiTwitch mt = mth.getMultiTwitch(channelName);
            if (mt != null)
            {
                return mt;
            }
        }
        return null;
    }

    public Mono<Void> sendMultiTwitchMessage(TwitchCommandEvent event)
    {
        MultiTwitch mt = getMultiTwitch(event.getEvent().getChannel().getName());
        if (mt != null)
        {
            mt.sendLinkToAllUsers(event.getEvent().getTwitchChat());
        }
        return event.empty();
    }

    public boolean checkLive(String twitchName)
    {
        for (Map<String, StreamData> group : streams.values())
        {
            if (group.containsKey(twitchName.toLowerCase()))
                return true;
        }
        return false;
    }

    public String getLastGame(String channel)
    {
        return twitchBot.getLastGame(channel);
    }
}
