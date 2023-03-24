package com.expiredminotaur.bcukbot.sql.collection.joke;

import com.expiredminotaur.bcukbot.sql.collection.CollectionService;
import org.springframework.stereotype.Service;


@Service
public class JokeService extends CollectionService<Joke>
{
    public JokeService(JokeRepository repository)
    {
        super(repository);
    }
}
