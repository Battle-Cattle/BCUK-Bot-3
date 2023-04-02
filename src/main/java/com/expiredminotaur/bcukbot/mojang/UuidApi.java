package com.expiredminotaur.bcukbot.mojang;

import com.expiredminotaur.bcukbot.HttpHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.net.URL;
import java.util.stream.Collectors;

public class UuidApi
{
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static NameWithUUID nameToUUID(String name)
    {
        try(BufferedReader br = HttpHandler.webRequest(new URL("https://api.mojang.com/users/profiles/minecraft/" + name)))
        {
            String output = br.lines().collect(Collectors.joining());
            return gson.fromJson(output, NameWithUUID.class);
        } catch (Exception e)
        {
            return null;
        }
    }

    public static Profile getProfile(String UUID)
    {
        try(BufferedReader br = HttpHandler.webRequest(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + UUID)))
        {
            String output = br.lines().collect(Collectors.joining());
            return gson.fromJson(output, Profile.class);
        } catch (Exception e)
        {
            return null;
        }
    }
}
