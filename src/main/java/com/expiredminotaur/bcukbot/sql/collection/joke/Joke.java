package com.expiredminotaur.bcukbot.sql.collection.joke;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.sql.Date;

@Entity
public class Joke
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String joke;
    private String source;
    private Date date;

    protected Joke()
    {
    }

    public Joke(String joke, String source)
    {
        this.joke = joke;
        this.source = source;
        date = new Date(System.currentTimeMillis());
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getJoke()
    {
        return joke;
    }

    public void setJoke(String joke)
    {
        this.joke = joke;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }
}
