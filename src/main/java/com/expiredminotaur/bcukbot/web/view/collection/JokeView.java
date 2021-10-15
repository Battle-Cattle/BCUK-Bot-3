package com.expiredminotaur.bcukbot.web.view.collection;

import com.expiredminotaur.bcukbot.sql.collection.joke.Joke;
import com.expiredminotaur.bcukbot.sql.collection.joke.JokeService;
import com.expiredminotaur.bcukbot.web.layout.MainLayout;
import com.expiredminotaur.bcukbot.web.security.UserTools;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "jokes", layout = MainLayout.class)
public class JokeView extends CollectionView<Joke>
{
    public JokeView(@Autowired UserTools userTools, @Autowired JokeService service)
    {
        super(userTools, service, Joke.class);
        setup("Jokes", "joke", "Joke");
    }
}