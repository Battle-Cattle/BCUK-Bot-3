package com.expiredminotaur.bcukbot;

public interface BotService
{
    void start();

    void stop();

    default void restart()
    {
        stop();
        start();
    }

    boolean isRunning();
}
