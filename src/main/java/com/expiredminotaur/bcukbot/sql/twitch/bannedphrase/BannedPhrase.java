package com.expiredminotaur.bcukbot.sql.twitch.bannedphrase;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class BannedPhrase
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    String phrase;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getPhrase()
    {
        return phrase;
    }

    public void setPhrase(String phrase)
    {
        this.phrase = phrase;
    }
}
