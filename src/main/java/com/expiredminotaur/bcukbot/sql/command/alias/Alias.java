package com.expiredminotaur.bcukbot.sql.command.alias;

import jakarta.persistence.Entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Alias
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    String shortCommand;
    String fullCommand;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getShortCommand()
    {
        return shortCommand;
    }

    public void setShortCommand(String shortCommand)
    {
        this.shortCommand = shortCommand;
    }

    public String getFullCommand()
    {
        return fullCommand;
    }

    public void setFullCommand(String fullCommand)
    {
        this.fullCommand = fullCommand;
    }
}
