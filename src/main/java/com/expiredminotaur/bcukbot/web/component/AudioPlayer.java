package com.expiredminotaur.bcukbot.web.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.server.StreamResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Tag("audio")
public class AudioPlayer extends Component
{
    private final Logger log = LoggerFactory.getLogger(AudioPlayer.class);

    public AudioPlayer(String path)
    {
        File sfx = new File(path);
        StreamResource resource = new StreamResource(sfx.getName(), () ->
        {
            try
            {
                return new FileInputStream(sfx);
            } catch (FileNotFoundException e)
            {
                log.error("Failed to load file {}", sfx.getPath());
                e.printStackTrace();
            }
            return null;
        });

        getElement().setAttribute("controls", true);
        getElement().setAttribute("src", resource);
    }
}