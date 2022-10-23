package com.expiredminotaur.bcukbot.twitch.command.chat;

import com.expiredminotaur.bcukbot.command.Commands;
import com.expiredminotaur.bcukbot.discord.music.MusicHandler;
import com.expiredminotaur.bcukbot.fun.dadjokes.JokeAPI;
import com.expiredminotaur.bcukbot.fun.tasks.TaskManager;
import com.expiredminotaur.bcukbot.justgiving.JustGivingAPI;
import com.expiredminotaur.bcukbot.sql.collection.joke.JokeUtils;
import com.expiredminotaur.bcukbot.sql.tasks.Punishment;
import com.expiredminotaur.bcukbot.sql.tasks.Task;
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

    @Autowired
    private TaskManager taskManager;
    //endregion

    public TwitchCommands()
    {
        commands.put("!Sfx", new TwitchCommand(this::sfxList, TwitchPermissions::everyone));
        commands.put("!SfxC", new TwitchCommand(e -> e.respond(sfxCategoriesList()), TwitchPermissions::everyone));
        commands.put("!SO", new TwitchCommand(this::shoutOut, TwitchPermissions::modPlus));
        commands.put("!DadJoke", new TwitchCommand(JokeAPI::jokeCommand, TwitchPermissions::everyone));
        commands.put("!Joke", new TwitchCommand(e -> jokeUtils.processCommand(e), TwitchPermissions::everyone));
        commands.put("!Playing", new TwitchCommand(this::playing, TwitchPermissions::everyone));
        commands.put("!Multi", new TwitchCommand(e -> liveStreamManager.sendMultiTwitchMessage(e), TwitchPermissions::everyone));
        commands.put("!TotalRaised", new TwitchCommand(e -> justGivingAPI.amountRaised(e), TwitchPermissions::everyone));
        commands.put("!task", new TwitchCommand(this::task, TwitchPermissions::everyone));
        commands.put("!321", new TwitchCommand(this::countdown, TwitchPermissions::everyone));
    }

    private Mono<Void> shoutOut(TwitchCommandEvent e)
    {
        String[] args = e.getFinalMessage().split(" ", 2);
        if (args.length == 2)
        {
            String channel = args[1].replace("@", ""); //Remove at in case someone dose @Username
            String game = liveStreamManager.getLastGame(channel);
            if (game != null && !game.isEmpty())
            {
                return e.multiRespond(liveStreamManager,
                        String.format("Go and check out %s's channel over at https://www.twitch.tv/%s last playing %s",
                                channel, channel, game));
            }
            return e.multiRespond(liveStreamManager,
                    String.format("Go and check out %s's channel over at https://www.twitch.tv/%s",
                            channel, channel));
        }
        return null;
    }

    private Mono<Void> playing(TwitchCommandEvent event)
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

    private Mono<Void> task(TwitchCommandEvent event)
    {
        Task task = taskManager.getTask();
        if (task != null)
            return event.respond(String.format("Current Task: %s", task.getTask()));
        Punishment punishment = taskManager.getPunishment();
        if (punishment != null)
            return event.respond(String.format("Current Punishment: %s", punishment.getPunishment()));
        return event.respond("No Active Task");
    }

    private Mono<Void> countdown(TwitchCommandEvent event)
    {
        Thread thread = new Thread(() ->
        {
            try
            {
                event.multiRespond(liveStreamManager, "3").block();
                Thread.sleep(1000);
                event.multiRespond(liveStreamManager, "2").block();
                Thread.sleep(1000);
                event.multiRespond(liveStreamManager, "1").block();
                Thread.sleep(1000);
                event.multiRespond(liveStreamManager, "GO!").block();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        });
        thread.start();
        return event.empty();
    }
}
