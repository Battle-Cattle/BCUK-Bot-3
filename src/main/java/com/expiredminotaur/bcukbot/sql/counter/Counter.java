package com.expiredminotaur.bcukbot.sql.counter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Counter
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    String triggerCommand;
    String checkCommand;
    String incrementMessage;
    String message;
    int currentValue = 0;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getTriggerCommand()
    {
        return triggerCommand;
    }

    public void setTriggerCommand(String triggerCommand)
    {
        this.triggerCommand = triggerCommand;
    }

    public String getCheckCommand()
    {
        return checkCommand;
    }

    public void setCheckCommand(String checkCommand)
    {
        this.checkCommand = checkCommand;
    }

    public String getIncrementMessage()
    {
        return incrementMessage;
    }

    public void setIncrementMessage(String incrementMessage)
    {
        this.incrementMessage = incrementMessage;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public int getCurrentValue()
    {
        return currentValue;
    }

    public void setCurrentValue(int newValue)
    {
        this.currentValue = newValue;
    }

    public void incrementCurrentValue()
    {
        currentValue++;
    }
}
