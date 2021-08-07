package com.expiredminotaur.bcukbot.discord.music;

import com.expiredminotaur.bcukbot.json.Settings;
import com.expiredminotaur.bcukbot.sql.sfx.SFX;
import com.expiredminotaur.bcukbot.sql.sfx.SFXRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Random;

@Component
public class SFXHandler
{
    private static final Random rng = new Random();
    private static long lastSFX = -1L;
    @Autowired
    private SFXRepository sfxRepository;
    @Autowired
    @Lazy
    private MusicHandler musicHandler;
    @Autowired
    private Settings settings;

    public void play(String trigger)
    {
        long time = System.currentTimeMillis();
        if (time - lastSFX > settings.getSfxDelay() * 1000L)
        {
            List<SFX> sfxList = sfxRepository.findByTriggerCommandIgnoreCase(trigger);
            if (sfxList.size() > 0)
            {
                SFX sound = pickSound(sfxList);
                musicHandler.loadAndPlayPriority(getFilePath(sound));
                lastSFX = time;
            }
        }
    }

    public void play(String trigger, boolean override)
    {
        if (override)
        {
            List<SFX> sfxList = sfxRepository.findByTriggerCommandIgnoreCase(trigger);
            if (sfxList.size() > 0)
            {
                SFX sound = pickSound(sfxList);
                musicHandler.loadAndPlayPriority(getFilePath(sound));
            }
        } else
            play(trigger);
    }

    private SFX pickSound(List<SFX> sounds)
    {
        int totalWeight = 0;
        for (SFX sound : sounds)
        {
            totalWeight += sound.getWeight();
        }
        int rand = rng.nextInt(totalWeight);
        int idx = 0;
        while (rand > sounds.get(idx).getWeight())
        {
            rand -= sounds.get(idx).getWeight();
            idx++;
        }
        return sounds.get(idx);
    }

    private String getFilePath(SFX sound)
    {
        return "." +
                File.separator +
                "sfx" +
                File.separator +
                sound.getFile();
    }
}
