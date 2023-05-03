package com.expiredminotaur.bcukbot.discord.music;

import com.expiredminotaur.bcukbot.discord.DiscordBot;
import com.expiredminotaur.bcukbot.json.Settings;
import com.expiredminotaur.bcukbot.sql.user.User;
import com.expiredminotaur.bcukbot.sql.user.UserRepository;
import com.expiredminotaur.bcukbot.twitch.TwitchBot;
import com.expiredminotaur.bcukbot.twitch.streams.LiveStreamManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Component
public class TrackScheduler extends AudioEventAdapter
{
    @Lazy
    @Autowired
    private DiscordBot discordBot;

    @Lazy
    @Autowired
    TwitchBot twitchBot;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Settings settings;

    @Autowired
    private LiveStreamManager liveStreamManager;

    private AudioPlayer player;
    private LinkedBlockingDeque<AudioTrack> queue;

    void setup(final AudioPlayer player)
    {
        this.player = player;
        player.addListener(this);
        this.queue = new LinkedBlockingDeque<>();
    }

    void queue(AudioTrack track)
    {
        if (!player.startTrack(track, true))
        {
            queue.offer(track);
        }
    }

    void playPriority(AudioTrack track)
    {
        player.setPaused(true);
        AudioTrack currentTrack = player.getPlayingTrack();
        TrackData currentTrackData = currentTrack.getUserData(TrackData.class);
        if (player.getPlayingTrack() != null && !currentTrackData.isSfx())
        {
            currentTrackData.setResume(true);
            AudioTrack clone = currentTrack.makeClone();
            clone.setPosition(currentTrack.getPosition());
            queue.offerFirst(clone);
        }
        queue.offerFirst(track);
        player.setVolume(settings.getSfxVolume());
        nextTrack();
        player.setPaused(false);
    }

    public void clear()
    {
        queue.clear();
    }

    public AudioTrack currentTrack()
    {
        return player.getPlayingTrack();
    }

    public void nextTrack()
    {
        discordBot.getGateway().updatePresence(ClientPresence.online()).subscribe();
        AudioTrack track = queue.poll();
        if (track == null || !track.getUserData(TrackData.class).isSfx())
        {
            player.setVolume(settings.getMusicVolume());
        }
        player.startTrack(track, false);
    }

    public BlockingQueue<AudioTrack> getPlaylist()
    {
        return queue;
    }

    private void sendMessageToDiscord(String songName, String message)
    {
        discordBot.getGateway().updatePresence(ClientPresence.online(ClientActivity.listening(songName))).subscribe();
        long channelId = settings.getSongAnnouncementChannel();
        if (channelId >= 0)
            discordBot.sendMessage(channelId, message);
    }

    private void sendMessageToTwitch(String message)
    {
        for (User user : userRepository.findByIsTwitchBotEnabledIsTrue())
        {
            if (liveStreamManager.checkLive(user.getTwitchName()))
            {
                twitchBot.sendMessage(user.getTwitchName(), message);
            }
        }
    }

    private void sendPlayingMessage(AudioTrack track)
    {
        TrackData trackData = track.getUserData(TrackData.class);
        String title = track.getInfo().title;
        String playing = "Playing: " + title;
        if (trackData.getRequestedBy() != null)
        {
            playing += " Requested by: " + track.getUserData(TrackData.class).getRequestedBy();
        }
        sendMessageToDiscord(title, playing);
        sendMessageToTwitch(playing);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track)
    {
        TrackData trackData = track.getUserData(TrackData.class);
        if (!trackData.isSfx() && !trackData.isResume())
        {
            sendPlayingMessage(track);
        }
        trackData.setResume(false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason)
    {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        discordBot.getGateway().updatePresence(ClientPresence.online()).subscribe();
        if (endReason.mayStartNext)
        {
            nextTrack();
        }
    }
}
