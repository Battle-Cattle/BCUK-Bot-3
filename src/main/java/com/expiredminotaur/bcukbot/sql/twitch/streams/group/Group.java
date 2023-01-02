package com.expiredminotaur.bcukbot.sql.twitch.streams.group;

import com.expiredminotaur.bcukbot.sql.twitch.streams.streamer.Streamer;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.Set;

@Entity(name = "stream_group")
public class Group
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private Long discordChannel = 0L;
    private boolean deleteOldPosts = true;
    private boolean multiTwitch = false;
    private String liveMessage = "%channel% is live playing %game% - %link%";
    private String newGameMessage = "%channel% is now playing %game% - %link%";
    private String multiTwitchMessage = "Multiple streams for %game% are live, watch them all at %link%";
    @OneToMany(
            mappedBy = "group",
            cascade = CascadeType.REMOVE,
            fetch = FetchType.EAGER
    )
    private Set<Streamer> streamers;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Long getDiscordChannel()
    {
        return discordChannel;
    }

    public void setDiscordChannel(Long discordChannel)
    {
        this.discordChannel = discordChannel;
    }

    public boolean isDeleteOldPosts()
    {
        return deleteOldPosts;
    }

    public void setDeleteOldPosts(boolean deleteOldPosts)
    {
        this.deleteOldPosts = deleteOldPosts;
    }

    public boolean isMultiTwitch()
    {
        return multiTwitch;
    }

    public void setMultiTwitch(boolean multiTwitch)
    {
        this.multiTwitch = multiTwitch;
    }

    public String getLiveMessage()
    {
        return liveMessage;
    }

    public void setLiveMessage(String liveMessage)
    {
        this.liveMessage = liveMessage;
    }

    public String getNewGameMessage()
    {
        return newGameMessage;
    }

    public void setNewGameMessage(String newGameMessage)
    {
        this.newGameMessage = newGameMessage;
    }

    public String getMultiTwitchMessage()
    {
        return multiTwitchMessage;
    }

    public void setMultiTwitchMessage(String multiTwitchMessage)
    {
        this.multiTwitchMessage = multiTwitchMessage;
    }

    public Set<Streamer> getStreamers()
    {
        return streamers;
    }

    public void setStreamers(Set<Streamer> streamers)
    {
        this.streamers = streamers;
    }
}
