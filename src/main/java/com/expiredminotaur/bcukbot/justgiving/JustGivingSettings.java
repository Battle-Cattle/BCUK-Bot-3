package com.expiredminotaur.bcukbot.justgiving;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JustGivingSettings
{
    @Transient
    private static final String fileName = "justgiving.json";
    private Boolean autoCheckEnabled = false;
    private Set<String> channels;
    private String appId;
    private String campaignName;
    private double lastTotal;
    private double lastTarget;
    private String message;
    private String facebookWebhook;
    private List<Long> discordChannelIds = new ArrayList<>();

    public static String getFileName()
    {
        return fileName;
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

    public List<Long> getDiscordChannelIds()
    {
        return discordChannelIds;
    }

    public void setDiscordChannelIds(List<Long> discordChannelIds)
    {
        this.discordChannelIds = discordChannelIds;
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
