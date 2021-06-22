package com.expiredminotaur.bcukbot.sql.sfx;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class SFX
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    String triggerCommand;
    String file;
    boolean hidden = true;
    int weight = 1;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private SFXCategory category;

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

    public String getFile()
    {
        return file;
    }

    public void setFile(String file)
    {
        this.file = file;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }

    public int getWeight()
    {
        return weight;
    }

    public void setWeight(int weight)
    {
        this.weight = weight;
    }

    public SFXCategory getCategory()
    {
        return category;
    }

    public void setCategory(SFXCategory category)
    {
        this.category = category;
    }
}
