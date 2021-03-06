package com.expiredminotaur.bcukbot.twitch.command.chat;

import com.expiredminotaur.bcukbot.command.Commands;
import com.expiredminotaur.bcukbot.discord.music.MusicHandler;
import com.expiredminotaur.bcukbot.fun.dadjokes.JokeAPI;
import com.expiredminotaur.bcukbot.justgiving.JustGivingAPI;
import com.expiredminotaur.bcukbot.sql.collection.joke.JokeUtils;
import com.expiredminotaur.bcukbot.twitch.streams.LiveStreamManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TwitchCommands extends Commands<TwitchCommandEvent>
{
    //region Autowired
    @Autowired
    private JokeUtils jokeUtils;

    @Autowired
    @Lazy
    private MusicHandler musicHandler;

    @Autowired
    private LiveStreamManager liveStreamManager;

    @Autowired
    private JustGivingAPI justGivingAPI;
    //endregion

    public TwitchCommands()
    {
        commands.put("!Sfx", new TwitchCommand(e -> e.respond(sfxList()), TwitchPermissions::everyone));
        commands.put("!SO", new TwitchCommand(this::shoutOut, TwitchPermissions::modPlus));
        commands.put("!DadJoke", new TwitchCommand(JokeAPI::jokeCommand, TwitchPermissions::everyone));
        commands.put("!Joke", new TwitchCommand(e -> jokeUtils.processCommand(e), TwitchPermissions::everyone));
        commands.put("!Playing", new TwitchCommand(this::playing, TwitchPermissions::everyone));
        commands.put("!Multi", new TwitchCommand(e -> liveStreamManager.getMultiTwitch(e), TwitchPermissions::everyone));
        commands.put("!GameBlastTotal", new TwitchCommand(e -> justGivingAPI.amountRaised(e), TwitchPermissions::everyone));
    }

    private Mono<Void> shoutOut(TwitchCommandEvent e)
    {
        String[] args = e.getFinalMessage().split(" ", 2);
        if (args.length == 2)
        {
            String channel = args[1].replace("@", ""); //Remove at in case someone dose @Username
            return e.respond(
                    String.format("Go and check out %s's channel over at https://www.twitch.tv/%s",
                            channel, channel));
        }
        return null;
    }

    public Mono<Void> playing(TwitchCommandEvent event)
    {
        AudioTrack track = musicHandler.getScheduler().currentTrack();
        if (track != null)
        {
            return event.respond("Playing: " + track.getInfo().title);
        } else
        {
            return event.respond("Nothing is playing");
        }
    }
}
