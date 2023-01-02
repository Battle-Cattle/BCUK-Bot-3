package com.expiredminotaur.bcukbot.justgiving;

import jakarta.persistence.Transient;
import java.util.Set;

public class JustGivingSettings
{
    @Transient
    static String fileName = "justgiving.json";
    Boolean autoCheckEnabled = false;
    Set<String> channels;
    String appId;
    String campaignName;
    double lastTotal;
    double lastTarget;
    String message;
    String facebookWebhook;
    long discordChannelId = -1L;

    public static String getFileName()
    {
        return fileName;
    }

    public static void setFileName(String fileName)
    {
        JustGivingSettings.fileName = fileName;
    }

    public Boolean getAutoCheckEnabled()
    {
        return autoCheckEnabled;
    }

    public void setAutoCheckEnabled(Boolean autoCheckEnabled)
    {
        this.autoCheckEnabled = autoCheckEnabled;
    }

    public Set<String> getChannels()
    {
        return channels;
    }

    public void setChannels(Set<String> channels)
    {
        this.channels = channels;
    }

    public String getAppId()
    {
        return appId;
    }

    public void setAppId(String appId)
    {
        this.appId = appId;
    }

    public String getCampaignName()
    {
        return campaignName;
    }

    public void setCampaignName(String campaignName)
    {
        this.campaignName = campaignName;
    }

    public double getLastTotal()
    {
        return lastTotal;
    }

    public void setLastTotal(double lastTotal)
    {
        this.lastTotal = lastTotal;
    }

    public double getLastTarget()
    {
        return lastTarget;
    }

    public void setLastTarget(double lastTarget)
    {
        this.lastTarget = lastTarget;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public long getDiscordChannelId()
    {
        return discordChannelId;
    }

    public void setDiscordChannelId(long discordChannelId)
    {
        this.discordChannelId = discordChannelId;
    }

    public String getFacebookWebhook()
    {
        return facebookWebhook;
    }

    public void setFacebookWebhook(String facebookWebhook)
    {
        this.facebookWebhook = facebookWebhook;
    }
}
