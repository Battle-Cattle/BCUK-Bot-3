package com.expiredminotaur.bcukbot.mojang;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

public class UuidApi
{
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static BufferedReader request(URL url) throws Exception
    {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (conn.getResponseCode() != 200)
        {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode() + " From:" + url.toString());
        }
        return new BufferedReader(new InputStreamReader((conn.getInputStream())));
    }

    public static NameWithUUID nameToUUID(String name)
    {
        try
        {
            BufferedReader br = request(new URL("https://api.mojang.com/users/profiles/minecraft/" + name));
            String output = br.lines().collect(Collectors.joining());
            return gson.fromJson(output, NameWithUUID.class);
        } catch (Exception e)
        {
            return null;
        }
    }

    public static Profile getProfile(String UUID)
    {
        try
        {
            BufferedReader br = request(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + UUID));
            String output = br.lines().collect(Collectors.joining());
            return gson.fromJson(output, Profile.class);
        } catch (Exception e)
        {
            return null;
        }
    }
}
