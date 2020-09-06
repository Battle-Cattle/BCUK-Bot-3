package com.expiredminotaur.bcukbot.web.view;

import com.expiredminotaur.bcukbot.sql.collection.quote.QuoteUtils;
import com.expiredminotaur.bcukbot.web.layout.MainLayout;
import com.expiredminotaur.bcukbot.web.security.UserTools;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "", layout = MainLayout.class)
public class MainView extends VerticalLayout
{
    public MainView(@Autowired UserTools userTools, @Autowired QuoteUtils quoteUtils)
    {
        long userID = userTools.getCurrentUsersID();
        String username = userTools.getCurrentUsersName();
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        String resolvedImage = VaadinService.getCurrent().resolveResource(
                "img/BCUK_BOT.png", VaadinSession.getCurrent().getBrowser());
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        Image logo = new Image(resolvedImage, "logo");
        Paragraph user = new Paragraph("Logged in as: " + username + " (" + userID + ")");
        Paragraph quote = new Paragraph(quoteUtils.random());

        add(logo, user, quote);
    }
}
