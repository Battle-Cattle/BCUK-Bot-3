package com.expiredminotaur.bcukbot.web.view.collection;

import com.expiredminotaur.bcukbot.sql.collection.clip.Clip;
import com.expiredminotaur.bcukbot.sql.collection.clip.ClipService;
import com.expiredminotaur.bcukbot.web.layout.MainLayout;
import com.expiredminotaur.bcukbot.web.security.UserTools;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "clips", layout = MainLayout.class)
public class ClipView extends CollectionView<Clip>
{
    public ClipView(@Autowired UserTools userTools, @Autowired ClipService service)
    {
        super(userTools, service, Clip.class);
        setup("Clips", "clip", "Clip");
    }
}
