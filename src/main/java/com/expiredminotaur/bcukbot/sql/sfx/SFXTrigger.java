package com.expiredminotaur.bcukbot.sql.sfx;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class SFXTrigger
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String triggerCommand;
    private boolean hidden;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private SFXCategory category;
    private String description;
    @OneToMany(
            mappedBy = "trigger",
            cascade = CascadeType.REMOVE,
            fetch = FetchType.EAGER
    )
    private Set<SFX> sfxSet = new HashSet<>();

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getTrigger()
    {
        return triggerCommand;
    }

    public void setTrigger(String trigger)
    {
        this.triggerCommand = trigger;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public SFXCategory getCategory()
    {
        return category;
    }

    public void setCategory(SFXCategory category)
    {
        this.category = category;
    }

    public Set<SFX> getSfxSet()
    {
        return sfxSet;
    }

    public void setSfxSet(Set<SFX> sfxSet)
    {
        this.sfxSet = sfxSet;
    }
}
