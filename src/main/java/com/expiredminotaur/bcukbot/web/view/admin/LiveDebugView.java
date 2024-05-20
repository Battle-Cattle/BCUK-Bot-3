package com.expiredminotaur.bcukbot.web.view.admin;

import com.expiredminotaur.bcukbot.Role;
import com.expiredminotaur.bcukbot.twitch.streams.LiveStreamManager;
import com.expiredminotaur.bcukbot.web.layout.MainLayout;
import com.expiredminotaur.bcukbot.web.security.AccessLevel;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Deprecated
    /*
    DEBUG CODE TO BE REMOVED LATER
     */
@Route(value = "live_debug", layout = MainLayout.class)
@AccessLevel(Role.ADMIN)
public class LiveDebugView extends VerticalLayout
{
    public LiveDebugView(@Autowired LiveStreamManager liveStreamManager)
    {
        liveStreamManager.debugGetStreams().forEach((groupName, data) ->
        {
            Div groupDiv = new Div();
            groupDiv.add(new H1(groupName));
            data.forEach((streamName, streamData) ->
            {
                Div streamDiv = new Div();
                streamDiv.add(new H2(streamName));
                streamDiv.add(new Paragraph(streamData.getGame()));
                streamDiv.add(new Paragraph(new Date(streamData.debugLastUpdated()).toString()));
                groupDiv.add(streamDiv);
            });
            add(groupDiv);
        });

        liveStreamManager.debugGetMultiTwitchHandlers().forEach((name, handler) ->
        {
            Div groupDiv = new Div();
            groupDiv.add(new H1(name));
            handler.debugMultiTwitchs().forEach((gameName, data) ->
            {
                Div multiDiv = new Div();
                multiDiv.add(new H2(gameName));
                multiDiv.add(new Paragraph(data.getLink()));
                if (data.debugMessage() == null)
                {
                    multiDiv.add("Message Unavailable");
                }else
                {
                    //We may need more info than this in the future, but it's a start
                    multiDiv.add(new Paragraph(data.debugMessage().toString()));
                }
                groupDiv.add(multiDiv);
            });
            add(groupDiv);
        });
    }
}
