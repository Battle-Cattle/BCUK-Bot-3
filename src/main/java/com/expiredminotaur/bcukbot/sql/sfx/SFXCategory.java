package com.expiredminotaur.bcukbot.sql.sfx;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.Set;

@Entity
public class SFXCategory
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    String name;
    @OneToMany(
            mappedBy = "category",
            cascade = CascadeType.REMOVE,
            fetch = FetchType.EAGER
    )
    private Set<SFX> sfxSet;

    public SFXCategory()
    {
    }

    public SFXCategory(String name)
    {
        this.name = name;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }


    public Set<SFX> getSfx()
    {
        return sfxSet;
    }
}
