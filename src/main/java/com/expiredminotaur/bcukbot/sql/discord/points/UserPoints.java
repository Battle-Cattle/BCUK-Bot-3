package com.expiredminotaur.bcukbot.sql.discord.points;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class UserPoints
{
    @Id
    private long discordUserId;
    private String lastUserName;
    private int points;
    private long lastPointsReceived;

    protected UserPoints()
    {
    }

    public UserPoints(long discordUserId)
    {
        this.discordUserId = discordUserId;
    }

    public Long getDiscordUserId()
    {
        return discordUserId;
    }

    public void setDiscordUserId(Long discordUserId)
    {
        this.discordUserId = discordUserId;
    }

    public String getLastUserName()
    {
        return lastUserName;
    }

    public void setLastUserName(String lastUserName)
    {
        this.lastUserName = lastUserName;
    }

    public int getPoints()
    {
        return points;
    }

    public void setPoints(int points)
    {
        this.points = points;
    }

    public long getLastPointsReceived()
    {
        return lastPointsReceived;
    }

    public void setLastPointsReceived(long lastPointsReceived)
    {
        this.lastPointsReceived = lastPointsReceived;
    }

    public void givePoints(int points)
    {
        this.points += points;
    }
}
