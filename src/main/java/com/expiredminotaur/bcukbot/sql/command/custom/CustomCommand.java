package com.expiredminotaur.bcukbot.sql.command.custom;

import com.expiredminotaur.bcukbot.sql.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.util.Set;

@Entity
public class CustomCommand
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int commandId;
    private String triggerString;
    private String output;
    private boolean isDiscordEnabled;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "twitch_user_commands",
            joinColumns = @JoinColumn(name = "command_id"),
            inverseJoinColumns = @JoinColumn(name = "discord_id"))
    private Set<User> twitchEnabledUsers;
    private boolean isMultiTwitch;

    public int getId()
    {
        return commandId;
    }

    public void setId(int commandId)
    {
        this.commandId = commandId;
    }

    public String getTrigger()
    {
        return triggerString;
    }

    public void setTrigger(String trigger)
    {
        this.triggerString = trigger;
    }

    public String getOutput()
    {
        return output;
    }

    public void setOutput(String output)
    {
        this.output = output;
    }

    public boolean isDiscordEnabled()
    {
        return isDiscordEnabled;
    }

    public void setDiscordEnabled(boolean discordEnabled)
    {
        isDiscordEnabled = discordEnabled;
    }

    public Set<User> getTwitchEnabledUsers()
    {
        return twitchEnabledUsers;
    }

    public void setTwitchEnabledUsers(Set<User> twitchEnabledUsers)
    {
        this.twitchEnabledUsers = twitchEnabledUsers;
    }

    public boolean isMultiTwitch()
    {
        return isMultiTwitch;
    }

    public void setMultiTwitch(boolean multiTwitch)
    {
        isMultiTwitch = multiTwitch;
    }
}
