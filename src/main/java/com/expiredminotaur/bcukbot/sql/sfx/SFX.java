package com.expiredminotaur.bcukbot.sql.sfx;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class SFX
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    String file;
    int weight = 1;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "trigger_id", referencedColumnName = "id")
    private SFXTrigger trigger;

    public SFX()
    {
    }

    public SFX(SFXTrigger trigger)
    {
        this.trigger = trigger;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getFile()
    {
        return file;
    }

    public void setFile(String file)
    {
        this.file = file;
    }

    public int getWeight()
    {
        return weight;
    }

    public void setWeight(int weight)
    {
        this.weight = weight;
    }

    public SFXTrigger getTrigger()
    {
        return trigger;
    }

    public void setTrigger(SFXTrigger trigger)
    {
        this.trigger = trigger;
    }
}
