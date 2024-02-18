package com.expiredminotaur.bcukbot.web.view.settings;

import com.expiredminotaur.bcukbot.Role;
import com.expiredminotaur.bcukbot.justgiving.JustGivingAPI;
import com.expiredminotaur.bcukbot.justgiving.JustGivingSettings;
import com.expiredminotaur.bcukbot.sql.user.User;
import com.expiredminotaur.bcukbot.sql.user.UserRepository;
import com.expiredminotaur.bcukbot.web.layout.MainLayout;
import com.expiredminotaur.bcukbot.web.security.AccessLevel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.stream.Collectors;

@Route(value = "justgiving", layout = MainLayout.class)
@AccessLevel(Role.MANAGER)
public class JustGivingView extends VerticalLayout
{
    private final Logger logger = LoggerFactory.getLogger(JustGivingView.class);

    public JustGivingView(@Autowired JustGivingAPI justGivingAPI, @Autowired UserRepository users)
    {
        Binder<JustGivingSettings> binder = new Binder<>(JustGivingSettings.class);

        Checkbox enabled = new Checkbox("Enabled");
        MultiSelectComboBox<String> channels = new MultiSelectComboBox<>("Twitch Channels");
        TextField discordChannel = new TextField("Discord Channel IDs (Separate with ;)");
        discordChannel.setWidthFull();
        TextField appId = new TextField("App ID");
        TextField campaignName = new TextField("Campaign Name");
        TextField campaignLink = new TextField("Campaign Link");
        campaignName.setWidthFull();
        TextField message = new TextField("Message");
        message.setWidthFull();
        TextField facebookWebhook = new TextField("Facebook Webhook");
        facebookWebhook.setWidthFull();

        channels.setItems(users.findByIsTwitchBotEnabledIsTrue().stream().map(User::getTwitchName).collect(Collectors.toList()));

        binder.bind(enabled, "autoCheckEnabled");
        binder.bind(channels, "channels");
        binder.forField(discordChannel).withConverter(s -> Arrays.stream(s.split(";")).mapToLong(Long::parseLong).boxed().collect(Collectors.toList()),
                        l -> l.stream().map(Object::toString).collect(Collectors.joining(";")))
                .bind("discordChannelIds");
        binder.bind(appId, "appId");
        binder.bind(campaignName, "campaignName");
        binder.bind(campaignLink, "campaignLink");
        binder.bind(message, "message");
        binder.bind(facebookWebhook, "facebookWebhook");

        Button save = new Button("Save", e ->
        {
            try
            {
                binder.writeBean(justGivingAPI.getSettings());
                justGivingAPI.saveSettings();
            } catch (ValidationException validationException)
            {
                logger.error("Justgiving settings error", validationException);
            }
        });

        NativeLabel test = new NativeLabel("Clear total raised to force twitch message and sfx");
        Button testButton = new Button("Test", e ->
        {
            justGivingAPI.getSettings().setLastTotal(0);
            justGivingAPI.saveSettings();
        });

        add(enabled, channels, discordChannel, appId, campaignName, campaignLink, message, facebookWebhook, save, test, testButton);
        binder.readBean(justGivingAPI.getSettings());
    }
}
