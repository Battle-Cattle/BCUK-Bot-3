package com.expiredminotaur.bcukbot.command;

import com.expiredminotaur.bcukbot.fun.counters.CounterHandler;
import com.expiredminotaur.bcukbot.sql.command.alias.Alias;
import com.expiredminotaur.bcukbot.sql.command.alias.AliasRepository;
import com.expiredminotaur.bcukbot.sql.sfx.SFX;
import com.expiredminotaur.bcukbot.sql.sfx.SFXCategoryRepository;
import com.expiredminotaur.bcukbot.sql.sfx.SFXRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public abstract class Commands<E extends CommandEvent<?>>
{
    protected final Map<String, Command<E>> commands = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    protected AliasRepository aliasRepository;
    protected CounterHandler counterHandler;
    protected SFXRepository sfxRepository;
    protected SFXCategoryRepository sfxCategoryRepository;

    public Mono<Void> processCommand(E event)
    {
        String message = event.getOriginalMessage();
        String[] command = message.split(" ", 2);

        List<Alias> alias = aliasRepository.findByTrigger(command[0]);
        if (alias.size() > 0)
        {
            String newCommand = alias.get(0).getFullCommand();
            command = newCommand.split(" ", 2);
            event.setAliased(newCommand);
        }

        if (commands.containsKey(command[0]))
        {
            Command<E> com = commands.get(command[0]);

            if (com.hasPermission(event))
            {
                return com.runTask(event);
            }
        }
        return event.empty();
    }

    protected Mono<Void> sfxList(@NotNull E event)
    {
        String[] command = event.getFinalMessage().split(" ", 2);
        List<SFX> list = (command.length < 2) ? sfxRepository.getSFXList() : sfxRepository.getSFXList(command[1].toLowerCase());
        if (list.size() > 0)
        {
            StringBuilder s = new StringBuilder();
            Set<String> triggers = new HashSet<>(); //Use a set here to remove duplicates
            list.forEach(sfx -> triggers.add(sfx.getTriggerCommand()));
            triggers.forEach(trigger -> s.append(trigger).append(", "));
            s.setLength(s.length() - 2);
            return event.respond(s.toString());
        }
        return event.respond("SFX list not found");
    }

    protected String sfxCategoriesList()
    {
        StringBuilder s = new StringBuilder();
        sfxCategoryRepository.findAll().forEach(category -> s.append(category.getName()).append(", "));
        s.setLength(s.length() - 2);
        return s.toString();
    }

    public List<String> getCommandList(E event)
    {
        return commands.entrySet().stream()
                .filter(c -> c.getValue().hasPermission(event))
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    @Autowired
    public final void setAliasRepository(AliasRepository aliasRepository)
    {
        this.aliasRepository = aliasRepository;
    }

    @Autowired
    public final void setCounterHandler(CounterHandler counterHandler)
    {
        this.counterHandler = counterHandler;
    }

    @Autowired
    public final void setSfxRepository(SFXRepository sfxRepository)
    {
        this.sfxRepository = sfxRepository;
    }

    @Autowired
    public void setSfxCategory(SFXCategoryRepository sfxCategoryRepository)
    {
        this.sfxCategoryRepository = sfxCategoryRepository;
    }
}