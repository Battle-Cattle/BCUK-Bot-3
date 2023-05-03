package com.expiredminotaur.bcukbot.discord.music;

public class TrackData
{
    private String requestedBy = null;
    private boolean sfx = false;

    public String getRequestedBy()
    {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy)
    {
        this.requestedBy = requestedBy;
    }

    public boolean isSfx()
    {
        return sfx;
    }

    public void setSfx(boolean sfx)
    {
        this.sfx = sfx;
    }
}
