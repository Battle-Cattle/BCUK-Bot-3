package com.expiredminotaur.bcukbot.sql.collection.joke;

import com.expiredminotaur.bcukbot.sql.collection.CollectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JokeUtils extends CollectionUtil
{
    @Autowired
    JokeRepository jokes;

    @Override
    public String add(String newEntry, String source)
    {
        if (!newEntry.trim().isEmpty())
        {
            Joke joke = new Joke(newEntry, source);
            joke = jokes.save(joke);
            return String.format("Added Joke %d: %s [%s]", joke.getId(), joke.getJoke(), joke.getDate());
        }
        return "No joke given";
    }

    @Override
    public String get(int id)
    {
        Optional<Joke> oJoke = jokes.findById(id);
        if (oJoke.isPresent())
        {
            Joke joke = oJoke.get();
            return String.format("Joke %d: %s [%s]", joke.getId(), joke.getJoke(), joke.getDate().toString());
        }
        throw new IndexOutOfBoundsException();
    }

    public String random()
    {
        List<Joke> list = jokes.findAll();
        long qty = list.size();
        if (qty > 0)
        {
            int idx = (int) (Math.random() * qty);
            Joke joke = list.get(idx);
            return String.format("Joke %d: %s [%s]", joke.getId(), joke.getJoke(), joke.getDate().toString());
        }
        return "No jokes in database";
    }
}
