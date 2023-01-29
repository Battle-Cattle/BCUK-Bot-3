package com.expiredminotaur.bcukbot.fun.dadjokes;

import com.expiredminotaur.bcukbot.HttpHandler;
import com.expiredminotaur.bcukbot.command.CommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.net.URL;

public class JokeAPI
{
    private static final Logger logger = LoggerFactory.getLogger(JokeAPI.class);

    private static String getJoke()
    {
        try(BufferedReader br = HttpHandler.textRequest(new URL("https://icanhazdadjoke.com/")))
        {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
            {
                output.append(line).append("    ");
            }
            return output.toString().trim();

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        logger.error("Failed to get Joke");
        return "!API ERROR!";
    }

    public static Mono<Void> jokeCommand(CommandEvent<?> event)
    {
        return event.respond(getJoke());
    }
}
