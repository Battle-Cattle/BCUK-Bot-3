package com.expiredminotaur.bcukbot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpHandler
{
    public static BufferedReader textRequest(URL url) throws Exception
    {
        HttpURLConnection conn = setupConnection(url);
        conn.setRequestProperty("Accept", "text/plain");
        return handleConnection(conn);
    }

    public static BufferedReader webRequest(URL url) throws Exception
    {
        HttpURLConnection conn = setupConnection(url);
        return handleConnection(conn);
    }

    public static BufferedReader postRequest(URL url) throws Exception
    {
        HttpURLConnection conn = setupPostConnection(url);
        conn.connect();
        return handleConnection(conn);
    }

    public static void postUTF8Request(URL url, String inputJson) throws Exception
    {
        HttpURLConnection conn = setupPostConnection(url);
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream())
        {
            byte[] input = inputJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        BufferedReader br = handleConnection(conn);
        if (br != null)
            br.close();
    }

    public static BufferedReader getRequest(URL url) throws Exception
    {
        HttpURLConnection conn = setupConnection(url);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        return handleConnection(conn);
    }

    private static HttpURLConnection setupConnection(URL url) throws Exception
    {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "https://github.com/Battle-Cattle/BCUK-Bot-3");
        return conn;
    }

    private static HttpURLConnection setupPostConnection(URL url) throws Exception
    {
        HttpURLConnection conn = setupConnection(url);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        return conn;
    }

    private static BufferedReader handleConnection(HttpURLConnection conn) throws Exception
    {
        return switch (conn.getResponseCode())
        {
            case 200 -> new BufferedReader(new InputStreamReader((conn.getInputStream())));
            case 304 -> null; //no change
            default ->
                    throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode() + " From:" + conn.getURL());
        };
    }
}
