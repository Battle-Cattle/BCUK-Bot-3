package com.expiredminotaur.bcukbot.web.view.collection;

import com.expiredminotaur.bcukbot.sql.collection.clip.Clip;
import com.expiredminotaur.bcukbot.sql.collection.clip.ClipRepository;
import com.expiredminotaur.bcukbot.web.layout.MainLayout;
import com.expiredminotaur.bcukbot.web.security.AccessLevel;
import com.expiredminotaur.bcukbot.web.security.UserTools;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@AccessLevel
@Route(value = "clips", layout = MainLayout.class)
public class ClipView extends CollectionView<Clip>
{
    public ClipView(@Autowired UserTools userTools, @Autowired ClipRepository repository)
    {
        super(userTools, repository, Clip.class);
        setup("Clips", "clip", "Clip");
    }
}
