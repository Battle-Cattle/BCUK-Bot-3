package com.expiredminotaur.bcukbot.twitch.streams;

import com.github.twitch4j.chat.TwitchChat;
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
        StringBuilder newLink = new StringBuilder();
        newLink.append("http://multitwitch.tv/");
        for (String user : users)
        {
            newLink.append(user);
            newLink.append("/");
        }
        link = newLink.toString();
    }

    public void sendLinkToAllUsers(TwitchChat chat)
    {
        sendToAllUsers(chat, link);
    }

    public void sendToAllUsers(TwitchChat chat, String message)
    {
        for (String channel : users)
            chat.sendMessage(channel, message);
    }
}