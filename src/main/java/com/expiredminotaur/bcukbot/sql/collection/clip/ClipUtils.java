package com.expiredminotaur.bcukbot.sql.collection.clip;

import com.expiredminotaur.bcukbot.sql.collection.CollectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ClipUtils extends CollectionUtil
{
    @Autowired
    private ClipRepository clips;

    @Override
    public String add(String newEntry, String source)
    {
        if (!newEntry.trim().isEmpty())
        {
            Clip clip = new Clip(newEntry, source);
            clip = clips.save(clip);
            return String.format("Added Clip %d: %s [%s]", clip.getId(), clip.getClip(), clip.getDate());
        }
        return "No clip given";
    }

    @Override
    public String get(int id)
    {
        Optional<Clip> oClip = clips.findById(id);
        if (oClip.isPresent())
        {
            Clip clip = oClip.get();
            return String.format("Clip %d: %s [%s]", clip.getId(), clip.getClip(), clip.getDate().toString());
        }
        throw new IndexOutOfBoundsException();
    }

    public String random()
    {
        List<Clip> list = clips.findAll();
        long qty = list.size();
        if (qty > 0)
        {
            int idx = (int) (Math.random() * qty);
            Clip clip = list.get(idx);
            return String.format("Clip %d: %s [%s]", clip.getId(), clip.getClip(), clip.getDate().toString());
        }
        return "No clips in database";
    }
}
