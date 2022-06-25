package com.expiredminotaur.bcukbot.web.view.login;

import com.expiredminotaur.bcukbot.discord.DiscordBot;
import com.expiredminotaur.bcukbot.sql.user.User;
import com.expiredminotaur.bcukbot.sql.user.UserRepository;
import com.expiredminotaur.bcukbot.web.security.UserTools;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.util.List;
import java.util.Optional;

@Route("login/process")
public class ProcessLoginView extends Div implements AfterNavigationObserver
{
    @Autowired
    private UserRepository users;

    @Autowired
    private UserTools userTools;

    @Autowired
    private DiscordBot discordBot;

    @Override
    public void afterNavigation(AfterNavigationEvent event)
    {
        long userID = userTools.getCurrentUsersID();

        Optional<User> oUser = users.findById(userID);
        String username = userTools.getCurrentUsersName();
        if (oUser.isPresent())
        {
            User user = oUser.get();
            user.setDiscordName(username);
            users.save(user);
            UI.getCurrent().getPage().setLocation("/");
        } else if (inBotServer(userID))
        {
            User user = new User();
            user.setDiscordId(userID);
            user.setDiscordName(username);
            users.save(user);
            UI.getCurrent().getPage().setLocation("/");
        } else
        {
            OAuth2AuthenticationToken auth = userTools.getAuthentication();
            if (auth != null)
                auth.setAuthenticated(false);
            UI.getCurrent().navigate(UnauthorisedView.class);
        }
    }

    private boolean inBotServer(long userId)
    {
        List<Guild> servers = discordBot.getGateway().getGuilds()
                .filterWhen(guild -> guild.getMembers().any(member -> member.getId().equals(Snowflake.of(userId))))
                .collectList().block();
        return servers != null && servers.size() > 0;
    }
}
