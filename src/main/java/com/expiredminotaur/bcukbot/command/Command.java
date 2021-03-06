package com.expiredminotaur.bcukbot.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * An abstract generic class to hold a command
 *
 * @param <E> Command input event type
 */
public abstract class Command<E extends CommandEvent<?>>
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Function<E, Mono<Void>> task;
    private final Function<E, Boolean> permission;

    /**
     * Constructor for a new command
     *
     * @param task       The function the command should run if the even has permission
     * @param permission The function that determine if the event has permission to run the task
     */
    public Command(Function<E, Mono<Void>> task, Function<E, Boolean> permission)
    {
        this.task = task;
        this.permission = permission;
    }

    /**
     * Checks the event has the required permissions
     *
     * @param event The event running the command
     * @return If the event has the required permissions
     */
    public Boolean hasPermission(E event)
    {
        return permission.apply(event);
    }

    /**
     * Runs the task for the command
     *
     * @param event The event running the command
     * @return The result of the command
     */
    public Mono<Void> runTask(E event)
    {
        if (!hasPermission(event))
        {
            log.warn("Command called without correct permissions");
            return event.empty();
        }
        return task.apply(event);
    }
}
