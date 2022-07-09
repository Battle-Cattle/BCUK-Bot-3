package com.expiredminotaur.bcukbot.web.view.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;

@Route("login")
public class LoginView extends VerticalLayout
{
    public LoginView()
    {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        String resolvedImage = VaadinService.getCurrent().resolveResource("img/BCUK_BOT.png");
        Image logo = new Image(resolvedImage, "logo");
        logo.setMaxWidth("100%");
        Button button = new Button("Login", e -> getUI().ifPresent(ui -> ui.getPage().setLocation("/oauth2/authorization/discord")));
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(logo, button);
    }
}
