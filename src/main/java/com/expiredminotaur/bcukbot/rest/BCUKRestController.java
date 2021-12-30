package com.expiredminotaur.bcukbot.rest;

import com.expiredminotaur.bcukbot.discord.DiscordBot;
import com.expiredminotaur.bcukbot.discord.music.MusicHandler;
import com.expiredminotaur.bcukbot.discord.music.SFXHandler;
import discord4j.core.object.entity.channel.VoiceChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BCUKRestController
{
    @Autowired
    SFXHandler sfxHandler;

    @Autowired
    DiscordBot discordBot;

    @Autowired
    MusicHandler musicHandler;

    @PostMapping("/playsfx")
    void playSFX(@RequestBody SfxRequest request)
    {
        sfxHandler.play(request.getSfx(), true);
    }

    @PostMapping("/join")
    void join(@RequestBody JoinRequest request)
    {
        discordBot.getChannel(request.getChannelId())
                .filter(c -> c instanceof VoiceChannel)
                .cast(VoiceChannel.class)
                .flatMap(c -> musicHandler.joinChannel(c)).subscribe();
    }

    @PostMapping("/leave")
    void leave()
    {
        musicHandler.leaveChannel();
    }

    @PostMapping("/volume")
    void volume(@RequestBody VolumeRequest request)
    {
        musicHandler.setVolume(request.getVolume());
    }

    @PostMapping("/pause")
    void pause()
    {
        musicHandler.togglePause();
    }
}
