package com.expiredminotaur.bcukbot.web.layout;

import com.expiredminotaur.bcukbot.Role;
import com.expiredminotaur.bcukbot.web.security.SecurityUtils;
import com.expiredminotaur.bcukbot.web.security.UserTools;
import com.expiredminotaur.bcukbot.web.view.MainView;
import com.expiredminotaur.bcukbot.web.view.MinecraftWhitelistView;
import com.expiredminotaur.bcukbot.web.view.MusicView;
import com.expiredminotaur.bcukbot.web.view.admin.DatabaseView;
import com.expiredminotaur.bcukbot.web.view.admin.LiveDebugView;
import com.expiredminotaur.bcukbot.web.view.admin.StreamBroadcasterView;
import com.expiredminotaur.bcukbot.web.view.admin.TaskManagerView;
import com.expiredminotaur.bcukbot.web.view.admin.UsersView;
import com.expiredminotaur.bcukbot.web.view.bot.DiscordBotView;
import com.expiredminotaur.bcukbot.web.view.bot.TwitchBotView;
import com.expiredminotaur.bcukbot.web.view.collection.ClipView;
import com.expiredminotaur.bcukbot.web.view.collection.JokeView;
import com.expiredminotaur.bcukbot.web.view.collection.QuoteView;
import com.expiredminotaur.bcukbot.web.view.commands.AliasView;
import com.expiredminotaur.bcukbot.web.view.commands.BannedPhrasesView;
import com.expiredminotaur.bcukbot.web.view.commands.CommandsView;
import com.expiredminotaur.bcukbot.web.view.commands.CountersView;
import com.expiredminotaur.bcukbot.web.view.commands.SFXManagerView;
import com.expiredminotaur.bcukbot.web.view.settings.JustGivingView;
import com.expiredminotaur.bcukbot.web.view.settings.MusicSettingsView;
import com.expiredminotaur.bcukbot.web.view.settings.StreamAnnouncementsView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.server.VaadinService;
import org.springframework.beans.factory.annotation.Autowired;

public class MainLayout extends AppLayout
{
    private final Div childWrapper = new Div();
    private final UserTools userTools;

    @Autowired
    public MainLayout(UserTools userTools)
    {
        this.userTools = userTools;
        MenuBar menu = new MenuBar();
        String resolvedImage = VaadinService.getCurrent().resolveResource("img/BCUK.png");
        Image logo = new Image(resolvedImage, "logo");
        menu.addThemeVariants(MenuBarVariant.LUMO_PRIMARY);

        menu.addItem("Home", e -> UI.getCurrent().navigate(MainView.class));
        setupCollections(menu.addItem("Collections").getSubMenu());
        addMenuItem(menu, "Music", MusicView.class);
        if (userTools.hasAccess(Role.MOD))
        {
            setupCommands(menu.addItem("Commands").getSubMenu());
        }
        if (userTools.hasAccess(Role.MANAGER))
        {
            setupBots(menu.addItem("Bots").getSubMenu());
        }
        addMenuItem(menu, "Minecraft Whitelist", MinecraftWhitelistView.class);
        if (userTools.hasAccess(Role.MANAGER))
        {
            setupSettings(menu.addItem("Settings").getSubMenu());
        }
        if (userTools.hasAccess(Role.ADMIN))
        {
            setupAdmin(menu.addItem("Admin").getSubMenu());
        }
        menu.addItem("Logout", e -> UI.getCurrent().getPage().setLocation("/logout"));

        addToNavbar(true, logo, menu);

        childWrapper.setHeightFull();

        setContent(childWrapper);
    }

    private void setupBots(SubMenu subMenu)
    {
        addMenuItem(subMenu, "Discord", DiscordBotView.class);
        addMenuItem(subMenu, "Twitch", TwitchBotView.class);
    }

    private void setupCommands(SubMenu subMenu)
    {
        addMenuItem(subMenu, "Commands", CommandsView.class);
        addMenuItem(subMenu, "Counters", CountersView.class);
        addMenuItem(subMenu, "SFX", SFXManagerView.class);
        addMenuItem(subMenu, "Alias", AliasView.class);
        addMenuItem(subMenu, "Banned Phrases", BannedPhrasesView.class);
    }

    private void setupCollections(SubMenu subMenu)
    {
        addMenuItem(subMenu, "Quotes", QuoteView.class);
        addMenuItem(subMenu, "Jokes", JokeView.class);
        addMenuItem(subMenu, "Clips", ClipView.class);
    }

    private void setupSettings(SubMenu subMenu)
    {
        addMenuItem(subMenu, "Stream Announcements", StreamAnnouncementsView.class);
        addMenuItem(subMenu, "Music", MusicSettingsView.class);
        addMenuItem(subMenu, "JustGiving", JustGivingView.class);
    }

    private void setupAdmin(SubMenu subMenu)
    {
        addMenuItem(subMenu, "Database", DatabaseView.class);
        addMenuItem(subMenu, "Users", UsersView.class);
        addMenuItem(subMenu, "Stream Broadcaster", StreamBroadcasterView.class);
        addMenuItem(subMenu, "Task Manager", TaskManagerView.class);
        addMenuItem(subMenu, "Live Debug", LiveDebugView.class);
    }

    private void addMenuItem(HasMenuItems menu, String name, Class<? extends Component> view)
    {
        if (SecurityUtils.isAccessGranted(view, userTools))
            menu.addItem(name, e -> UI.getCurrent().navigate(view));
    }

    @Override
    public void showRouterLayoutContent(HasElement content)
    {
        childWrapper.getElement().appendChild(content.getElement());
    }
}
