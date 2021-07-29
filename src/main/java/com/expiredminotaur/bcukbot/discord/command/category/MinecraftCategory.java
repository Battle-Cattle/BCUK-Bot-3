package com.expiredminotaur.bcukbot.discord.command.category;

import com.expiredminotaur.bcukbot.discord.command.DiscordCommand;
import com.expiredminotaur.bcukbot.discord.command.DiscordPermissions;
import com.expiredminotaur.bcukbot.discord.command.MinecraftCommands;
import discord4j.common.util.Snowflake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MinecraftCategory extends Category
{
    private MinecraftCommands minecraftCommands;

    public MinecraftCategory()
    {
        commands.put("!Whitelist", new DiscordCommand(e -> minecraftCommands.whitelist(e), event -> DiscordPermissions.hasRole(event, Snowflake.of(489887389725229066L))));
    }

    @Autowired
    public final void setMinecraftCommands(MinecraftCommands minecraftCommands)
    {
        this.minecraftCommands = minecraftCommands;
    }

    @Override
    public String getName()
    {
        return "MINECRAFT";
    }
}
