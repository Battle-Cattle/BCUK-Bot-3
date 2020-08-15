package com.expiredminotaur.bcukbot.discord.music;

import com.expiredminotaur.bcukbot.discord.DiscordBot;
import com.expiredminotaur.bcukbot.json.Settings;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
public class MusicHandler
{
    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final discord4j.voice.AudioProvider provider;
    private final TrackScheduler scheduler;
    private final Settings settings;

    public MusicHandler(@Autowired DiscordBot discordBot, @Autowired Settings settings)
    {
        this.settings = settings;
        playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        registerSources();
        player = playerManager.createPlayer();
        player.setVolume(settings.getMusicVolume());
        scheduler = new TrackScheduler(player, discordBot, settings);
        provider = new AudioProvider(player);
    }

    private void registerSources()
    {
        YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager(true);
        youtubeAudioSourceManager.setPlaylistPageCount(100);
        playerManager.registerSourceManager(youtubeAudioSourceManager);

        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new BeamAudioSourceManager());
        playerManager.registerSourceManager(new GetyarnAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager());
        playerManager.registerSourceManager(new LocalAudioSourceManager());
    }

    public discord4j.voice.AudioProvider getProvider()
    {
        return provider;
    }

    public TrackScheduler getScheduler()
    {
        return scheduler;
    }

    public Mono<Void> listTracks(MessageCreateEvent event, int page)
    {
        int length = scheduler.getPlaylist().size();
        int pages = length / 10 + (length % 10 > 0 ? 1 : 0);
        page = Math.min(page, pages);
        if (length > 0)
        {
            StringBuilder s = new StringBuilder();
            AudioTrack[] tracks = new AudioTrack[]{};
            tracks = ArrayUtils.subarray(scheduler.getPlaylist().toArray(tracks), (page - 1) * 10, page * 10);
            int i = 1;
            s.append(String.format("Page %d of %d\n", page, pages));
            for (AudioTrack track : tracks)
            {
                s.append(String.format("[%d] ", (page - 1) * 10 + i++));
                s.append(track.getInfo().title);
                s.append("\n");
            }
            return event.getMessage().getChannel().flatMap(mc -> mc.createMessage(s.toString())).then();
        } else
        {
            return event.getMessage().getChannel().flatMap(mc -> mc.createMessage("No Songs in playlist")).then();
        }
    }

    public void loadAndPlay(MessageCreateEvent event, final String trackUrl)
    {
        playerManager.loadItemOrdered(player, trackUrl, new AudioLoadResultHandler()
        {
            @Override
            public void trackLoaded(AudioTrack track)
            {
                event.getMessage().getChannel().flatMap(mc -> mc.createMessage("Adding to queue: " + track.getInfo().title)).subscribe();
                if (event.getMember().isPresent())
                    track.setUserData(event.getMember().get().getDisplayName());
                scheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist)
            {
                List<AudioTrack> tracks = playlist.getTracks();
                if (playlist.getName().startsWith("Search results for:"))
                {
                    AudioTrack track = tracks.get(0);
                    if (event.getMember().isPresent())
                        track.setUserData(event.getMember().get().getDisplayName());
                    scheduler.queue(track);
                    event.getMessage().getChannel().flatMap(mc -> mc.createMessage("Adding to queue: " + track.getInfo().title)).subscribe();
                } else
                {
                    Collections.shuffle(tracks);
                    for (AudioTrack track : tracks)
                    {
                        if (event.getMember().isPresent())
                            track.setUserData(event.getMember().get().getDisplayName());
                        scheduler.queue(track);
                    }
                    event.getMessage().getChannel().flatMap(mc -> mc.createMessage("Adding playlist to queue: " + playlist.getName())).subscribe();
                }
            }

            @Override
            public void noMatches()
            {
                event.getMessage().getChannel().flatMap(mc -> mc.createMessage("Nothing found by " + trackUrl)).subscribe();
            }

            @Override
            public void loadFailed(FriendlyException exception)
            {
                event.getMessage().getChannel().flatMap(mc -> mc.createMessage("Could not play: " + exception.getMessage())).subscribe();
            }
        });
    }

    public void loadAndPlayPriority(final String trackUrl)
    {
        playerManager.loadItemOrdered(player, trackUrl, new AudioLoadResultHandler()
        {
            @Override
            public void trackLoaded(AudioTrack track)
            {
                scheduler.playPriority(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist)
            {
                new Exception("SFX tried to play as playlist").printStackTrace();
            }

            @Override
            public void noMatches()
            {
                new Exception("Missing SFX").printStackTrace();
            }

            @Override
            public void loadFailed(FriendlyException exception)
            {
                exception.printStackTrace();
            }
        });
    }

    public Mono<Void> setVolume(MessageCreateEvent event, String volume)
    {
        int vol = getInt(volume);
        if (vol >= 0 && vol <= 100)
        {
            player.setVolume(vol);
            settings.setMusicVolume(vol);
            return getVolume(event);
        } else
        {
            return event.getMessage().getChannel().flatMap(mc -> mc.createMessage("Please enter a number between 0 and 100")).then();
        }
    }

    //TODO twitch set volume
    /*
    public void setVolume(WhisperEvent event, String volume)
    {
        int vol = Utils.getInt(volume);
        if (vol >= 0 && vol <= 100)
        {
            player.setVolume(Integer.parseInt(vol));
            MusicConfig.getInstance().setVolume(Integer.parseInt(vol));
            getVolume(event);
        } else
        {
            event.sendReply("Please enter a number between 0 and 100");
        }
    }
     */

    public Mono<Void> getVolume(MessageCreateEvent event)
    {
        return event.getMessage().getChannel().flatMap(mc -> mc.createMessage("Volume set to " + player.getVolume())).then();
    }

    //TODO twitch get volume
    /*
    public static void getVolume(WhisperEvent event)
    {
        event.sendReply("Volume set to " + player.getVolume());
    }
     */

    public Mono<Void> togglePause(MessageCreateEvent event)
    {
        player.setPaused(!player.isPaused());

        if (player.isPaused())
        {
            return event.getMessage().getChannel().flatMap(mc -> mc.createMessage("Music Paused")).then();
        } else
        {
            return event.getMessage().getChannel().flatMap(mc -> mc.createMessage("Music Resumed")).then();
        }
    }

    //TODO twitch pause
    /*
    public void togglePause(WhisperEvent event)
    {
        player.setPaused(!player.isPaused());

        if (player.isPaused())
        {
            event.sendReply("Music Paused");
        } else
        {
            event.sendReply("Music Resumed");
        }
    }
     */

    private int getInt(String s)
    {
        try
        {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe)
        {
            return -1;
        }
    }
}
