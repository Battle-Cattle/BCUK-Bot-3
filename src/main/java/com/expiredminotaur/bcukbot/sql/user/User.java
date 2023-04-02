package com.expiredminotaur.bcukbot.sql.user;

import com.expiredminotaur.bcukbot.Role;
import com.expiredminotaur.bcukbot.sql.command.custom.CustomCommand;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import java.util.Set;

@Entity
public class User
{
    @Id
    private Long discordId;
    private String discordName;
    private String twitchName;
    private String twitchOAuth;
    private int accessLevel;
    private boolean isTwitchBotEnabled = false;
    @ManyToMany(mappedBy = "twitchEnabledUsers", fetch = FetchType.EAGER)
    private Set<CustomCommand> EnabledTwitchCommands;

    public Long getDiscordId()
    {
        return discordId;
    }

    public void setDiscordId(Long discordId)
    {
        this.discordId = discordId;
    }

    public String getDiscordName()
    {
        return discordName;
    }

    public void setDiscordName(String discordName)
    {
        this.discordName = discordName;
    }

    public String getTwitchName()
    {
        return twitchName;
    }

    public void setTwitchName(String twitchName)
    {
        this.twitchName = twitchName;
    }

    public String getTwitchOAuth()
    {
        return twitchOAuth;
    }

    public void setTwitchOAuth(String twitchOAuth)
    {
        this.twitchOAuth = twitchOAuth;
    }

    public Role getAccessLevel()
    {
        return Role.valueOf(accessLevel);
    }

    public void setAccessLevel(Role accessLevel)
    {
        this.accessLevel = accessLevel.getValue();
    }

    public boolean isTwitchBotEnabled()
    {
        return isTwitchBotEnabled;
    }

    public void setTwitchBotEnabled(boolean twitchBotEnabled)
    {
        isTwitchBotEnabled = twitchBotEnabled;
    }

    public Set<CustomCommand> getEnabledTwitchCommands()
    {
        return EnabledTwitchCommands;
    }

    public void setEnabledTwitchCommands(Set<CustomCommand> enabledTwitchCommands)
    {
        EnabledTwitchCommands = enabledTwitchCommands;
    }
}
