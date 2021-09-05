package com.expiredminotaur.bcukbot.web.view.commands;

import com.expiredminotaur.bcukbot.Role;
import com.expiredminotaur.bcukbot.sql.command.custom.CommandRepository;
import com.expiredminotaur.bcukbot.sql.command.custom.CustomCommand;
import com.expiredminotaur.bcukbot.sql.user.User;
import com.expiredminotaur.bcukbot.sql.user.UserRepository;
import com.expiredminotaur.bcukbot.web.component.Form;
import com.expiredminotaur.bcukbot.web.layout.MainLayout;
import com.expiredminotaur.bcukbot.web.security.AccessLevel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.util.stream.Collectors;

@Route(value = "commands", layout = MainLayout.class)
@AccessLevel(Role.MOD)
public class CommandsView extends HorizontalLayout
{
    private final UserRepository users;
    private final CommandRepository commands;
    private final Grid<CustomCommand> grid;


    public CommandsView(@Autowired UserRepository users, @Autowired CommandRepository commands)
    {
        this.users = users;
        this.commands = commands;

        setSizeFull();

        TextField trigger = new TextField("Trigger");
        TextField output = new TextField("Output");
        output.setWidthFull();
        Checkbox discord = new Checkbox("Enable on Discord");
        MultiselectComboBox<User> twitchUsers = new MultiselectComboBox<>("Users");
        twitchUsers.setItemLabelGenerator(User::getTwitchName);
        twitchUsers.setItems(users.findByIsTwitchBotEnabledIsTrue());

        Binder<CustomCommand> binder = new Binder<>(CustomCommand.class);
        binder.bind(trigger, "trigger");
        binder.bind(output, "output");
        binder.bind(discord, "discordEnabled");
        binder.bind(twitchUsers, "twitchEnabledUsers");

        grid = new Grid<>(CustomCommand.class);
        grid.setItems(commands.findAll());
        grid.setColumns("trigger", "output", "discordEnabled");
        grid.addColumn(c -> c.getTwitchEnabledUsers().stream().map(User::getTwitchName).collect(Collectors.joining(", "))).setHeader("Twitch Enabled");
        EditForm editForm = new EditForm();
        grid.addColumn(new ComponentRenderer<>(c -> new Button("Edit", e -> editForm.open(c)))).setFlexGrow(0).setHeader("Edit");
        grid.getColumns().forEach(c -> c.setAutoWidth(true));
        grid.recalculateColumnWidths();

        Button add = new Button("Add", e ->
        {
            try
            {
                CustomCommand command = new CustomCommand();
                binder.writeBean(command);
                commands.save(command);
                binder.readBean(new CustomCommand());
                grid.setItems(commands.findAll());
                grid.recalculateColumnWidths();
            } catch (ValidationException validationException)
            {
                validationException.printStackTrace();
            }
        });

        VerticalLayout left = new VerticalLayout(trigger, trigger, output, discord, twitchUsers, add);
        left.setSizeFull();
        left.setWidth("25%");
        VerticalLayout right = new VerticalLayout(grid);
        right.setSizeFull();
        add(left, right);
        setFlexGrow(1, right);
    }

    private class EditForm extends Form<CustomCommand>
    {

        public EditForm()
        {
            super(CustomCommand.class);
            addField("Trigger", new TextField(), "trigger").setWidthFull();
            addField("Output", new TextField(), "output").setWidthFull();
            addField("Discord Enabled", new Checkbox(), "discordEnabled");
            MultiselectComboBox<User> twitchUsers = addField("Users", new MultiselectComboBox<>(), "twitchEnabledUsers");
            twitchUsers.setItemLabelGenerator(User::getTwitchName);
            twitchUsers.setItems(users.findByIsTwitchBotEnabledIsTrue());
        }

        @Override
        protected void saveData(CustomCommand data)
        {
            commands.save(data);
            grid.setItems(commands.findAll());
            grid.recalculateColumnWidths();
        }
    }
}
