package com.expiredminotaur.bcukbot.twitch.streams;

import com.expiredminotaur.bcukbot.twitch.TwitchBot;
import discord4j.core.object.entity.Message;

import java.util.Set;

public class MultiTwitch
{
    private Set<String> users;
    private Message message;
    private String link;

    MultiTwitch(Set<String> users)
    {
        this.users = users;
        updateLink();
    }

    public Set<String> getUsers()
    {
        return users;
    }

    public void setUsers(Set<String> users)
    {
        this.users = users;
        updateLink();
    }

    public void setMessage(Message message)
    {
        this.message = message;
    }

    public void deleteMessage()
    {
        if (message != null)
            message.delete("Offline Stream").subscribe();
        message = null;
    }

    public String getLink()
    {
        return link;
    }

    private void updateLink()
    {
        StringBuilder link = new StringBuilder();
        link.append("http://multitwitch.tv/");
        for (String user : users)
        {
            link.append(user);
            link.append("/");
        }
        this.link = link.toString();
    }

    public void sendToAllUsers(TwitchBot twitchBot)
    {
        for (String channel : users)
            twitchBot.sendMessage(channel, link);
    }
}