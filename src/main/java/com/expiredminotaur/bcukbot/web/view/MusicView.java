package com.expiredminotaur.bcukbot.web.view;

import com.expiredminotaur.bcukbot.discord.music.MusicHandler;
import com.expiredminotaur.bcukbot.discord.music.TrackData;
import com.expiredminotaur.bcukbot.web.layout.MainLayout;
import com.expiredminotaur.bcukbot.web.security.AccessLevel;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@AccessLevel
@Route(value = "music", layout = MainLayout.class)
public class MusicView extends VerticalLayout
{
    public MusicView(@Autowired MusicHandler musicHandler)
    {
        setSizeFull();

        AudioTrack currentTrack = musicHandler.getScheduler().currentTrack();
        String playing = currentTrack != null ? "Playing: " + currentTrack.getInfo().title : "Nothing is Playing";
        H2 playingHeader = new H2(playing);
        BlockingQueue<AudioTrack> list = musicHandler.getScheduler().getPlaylist();
        Grid<AudioTrack> grid = new Grid<>();
        grid.setItems(list);
        grid.addColumn(track -> track.getInfo().title).setHeader("Title");
        grid.addColumn(this::getLength).setHeader("Length");
        grid.addColumn(new ComponentRenderer<>(track -> new Anchor(track.getInfo().uri, track.getInfo().uri))).setHeader("Link");
        grid.addColumn(track -> track.getUserData(TrackData.class).getRequestedBy()).setHeader("Requested By");
        grid.getColumns().forEach(c -> c.setAutoWidth(true));
        grid.recalculateColumnWidths();
        add(playingHeader, grid);
    }

    private String getLength(AudioTrack track)
    {
        long millis = track.getInfo().length;
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }
}
