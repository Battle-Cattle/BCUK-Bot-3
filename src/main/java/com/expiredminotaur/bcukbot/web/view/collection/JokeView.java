package com.expiredminotaur.bcukbot.web.view.collection;

import com.expiredminotaur.bcukbot.sql.collection.joke.Joke;
import com.expiredminotaur.bcukbot.sql.collection.joke.JokeRepository;
import com.expiredminotaur.bcukbot.web.layout.MainLayout;
import com.expiredminotaur.bcukbot.web.security.AccessLevel;
import com.expiredminotaur.bcukbot.web.security.UserTools;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@AccessLevel
@Route(value = "jokes", layout = MainLayout.class)
public class JokeView extends CollectionView<Joke>
{
    public JokeView(@Autowired UserTools userTools, @Autowired JokeRepository repository)
    {
        super(userTools, repository, Joke.class);
        setup("Jokes", "joke", "Joke");
    }
}