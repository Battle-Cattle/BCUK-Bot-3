package com.expiredminotaur.bcukbot.discord.music;

import com.expiredminotaur.bcukbot.json.Settings;
import com.expiredminotaur.bcukbot.sql.sfx.SFX;
import com.expiredminotaur.bcukbot.sql.sfx.SFXTrigger;
import com.expiredminotaur.bcukbot.sql.sfx.SFXTriggerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Component
public class SFXHandler
{
    private static final Random rng = new Random();
    private static long lastSFX = -1L;
    @Autowired
    private SFXTriggerRepository sfxTriggerRepository;
    @Autowired
    @Lazy
    private MusicHandler musicHandler;
    @Autowired
    private Settings settings;

    public void play(String cmd)
    {
        long time = System.currentTimeMillis();
        if (time - lastSFX > settings.getSfxDelay() * 1000L)
        {
            SFXTrigger trigger = sfxTriggerRepository.findByTriggerCommandIgnoreCase(cmd);
            if (trigger != null)
            {
                Set<SFX> sfxList = trigger.getSfxSet();
                if (!sfxList.isEmpty())
                {
                    SFX sound = pickSound(sfxList.stream().toList());
                    musicHandler.loadAndPlayPriority(getFilePath(sound));
                    lastSFX = time;
                }
            }
        }
    }

    public void play(String trigger, boolean override)
    {
        if (override)
        {
            Set<SFX> sfxList = sfxTriggerRepository.findByTriggerCommandIgnoreCase(trigger).getSfxSet();
            if (!sfxList.isEmpty())
            {
                SFX sound = pickSound(sfxList.stream().toList());
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
        while (rand >= sounds.get(idx).getWeight())
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
