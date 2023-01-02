package com.expiredminotaur.bcukbot.sql.minecraft;

import com.expiredminotaur.bcukbot.mojang.Profile;
import com.expiredminotaur.bcukbot.mojang.UuidApi;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(catalog = "bcuk_mc")
public class Whitelist
{
    @Id
    private Long discordID;
    private String mcUUID;
    @Transient
    private String mcName = null;

    protected Whitelist()
    {
    }

    public Whitelist(Long discordID, String mcUUID)
    {
        this.discordID = discordID;
        this.mcUUID = mcUUID;
    }

    public Long getDiscordID()
    {
        return discordID;
    }

    public void setDiscordID(Long discordID)
    {
        this.discordID = discordID;
    }

    public String getMcUUID()
    {
        return mcUUID;
    }

    public void setMcUUID(String mcUUID)
    {
        this.mcUUID = mcUUID;
        updateName();
    }

    public String getMcName()
    {
        if (mcName == null)
            updateName();
        return mcName;
    }

    private void updateName()
    {
        Profile profile = UuidApi.getProfile(mcUUID);
        mcName = (profile == null) ? null : profile.getName();
    }
}
