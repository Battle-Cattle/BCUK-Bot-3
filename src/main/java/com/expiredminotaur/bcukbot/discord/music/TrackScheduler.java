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
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Component
public class TrackScheduler extends AudioEventAdapter
{
    @Autowired
    private DiscordBot discordBot;

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

    private boolean resume = false;

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
        if (player.getPlayingTrack() != null && !player.getPlayingTrack().getUserData(TrackData.class).isSfx())
        {
            AudioTrack clone = player.getPlayingTrack().makeClone();
            clone.setPosition(player.getPlayingTrack().getPosition());
            queue.offerFirst(clone);
            resume = true;
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
        discordBot.getGateway().updatePresence(Presence.online()).subscribe();
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

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track)
    {
        if (!track.getUserData(TrackData.class).isSfx())
        {
            if (!resume)
            {
                discordBot.getGateway().updatePresence(Presence.online(Activity.listening(currentTrack().getInfo().title))).subscribe();

                String playing = "Playing: " + track.getInfo().title;
                if (track.getUserData(TrackData.class).getRequestedBy() != null)
                {
                    playing += " Requested by: " + track.getUserData(TrackData.class).getRequestedBy();
                }

                long channelId = settings.getSongAnnouncementChannel();
                if (channelId >= 0)
                    discordBot.sendMessage(channelId, playing);

                for (User user : userRepository.findByIsTwitchBotEnabledIsTrue())
                {
                    if (liveStreamManager.checkLive(user.getTwitchName()))
                    {
                        twitchBot.sendMessage(user.getTwitchName(), playing);
                    }
                }
            } else
            {
                resume = false;
            }
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason)
    {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        discordBot.getGateway().updatePresence(Presence.online()).subscribe();
        if (endReason.mayStartNext)
        {
            nextTrack();
        }
    }
}
