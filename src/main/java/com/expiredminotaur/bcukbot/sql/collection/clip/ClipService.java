package com.expiredminotaur.bcukbot.sql.collection.clip;

import com.expiredminotaur.bcukbot.sql.collection.CollectionService;
import org.springframework.stereotype.Service;

@Service
public class ClipService extends CollectionService<Clip>
{
    public ClipService(ClipRepository repository)
    {
        super(repository);
    }
}
