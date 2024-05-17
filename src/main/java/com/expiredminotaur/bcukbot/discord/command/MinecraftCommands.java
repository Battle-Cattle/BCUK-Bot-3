package com.expiredminotaur.bcukbot.discord.command;

import com.expiredminotaur.bcukbot.mojang.NameWithUUID;
import com.expiredminotaur.bcukbot.mojang.Profile;
import com.expiredminotaur.bcukbot.mojang.UuidApi;
import com.expiredminotaur.bcukbot.sql.minecraft.Whitelist;
import com.expiredminotaur.bcukbot.sql.minecraft.WhitelistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class MinecraftCommands
{
    @Autowired
    WhitelistRepository whitelistRepository;

    public Mono<Void> whitelist(DiscordCommandEvent event)
    {
        AtomicReference<String> message = new AtomicReference<>("");
        String content = event.getFinalMessage();
        if (!content.isEmpty())
        {
            String[] splitMessage = content.split(" ");
            if (splitMessage.length < 2)
                return event.respond("Username not given");
            event.getEvent().getMember().ifPresent(member -> message.set(addMinecraftWhitelist(member.getId().asLong(), splitMessage[1])));
            if (!message.get().isEmpty())
                return event.respond(message.get());
        }
        return event.empty();
    }

    private String addMinecraftWhitelist(Long discordID, String username)
    {
        NameWithUUID newUser = UuidApi.nameToUUID(username);
        if (newUser != null)
        {
            Optional<Whitelist> current = whitelistRepository.findById(discordID);
            if (current.isPresent())
            {
                Whitelist user = current.get();
                String removed = user.getMcUUID();
                Profile removedUser = UuidApi.getProfile(removed);
                user.setMcUUID(newUser.getId());
                whitelistRepository.save(user);
                if (removedUser != null)
                {
                    return "Replaced " + removedUser.getName() + " with " + newUser.getName() + " in the whitelist, You can only whitelist one account";
                }
                return "Replaced ?Unknown? with " + newUser.getName() + " in the whitelist, You can only whitelist one account";
            } else
            {
                Whitelist user = new Whitelist(discordID, newUser.getId());
                whitelistRepository.save(user);
                return "Added " + newUser.getName() + " to the whitelist";
            }
        }
        return "Minecraft user not found";
    }
}
