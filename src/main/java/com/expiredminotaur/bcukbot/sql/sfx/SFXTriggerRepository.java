package com.expiredminotaur.bcukbot.sql.sfx;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;

public interface SFXTriggerRepository extends CrudRepository<SFXTrigger, Integer>
{
    @Override
    @NotNull List<SFXTrigger> findAll();

    @Query("from SFXTrigger where hidden=false and category is null")
    @Cacheable(value = "SFX_TRIGGER_LIST")
    @NotNull Set<SFXTrigger> getSFXList();

    @Cacheable(value = "SFX_TRIGGER_NULL")
    @NotNull List<SFXTrigger> findByCategoryIsNull();

    @Cacheable(value = "SFX_TRIGGER")
    SFXTrigger findByTriggerCommandIgnoreCase(String trigger);

    @Cacheable(value = "SFX_TRIGGER")
    @NotNull List<SFXTrigger> findByCategory(SFXCategory category);

    @Override
    @CacheEvict(value = {"SFX", "SFX_TRIGGER", "SFX_TRIGGER_LIST", "SFX_TRIGGER_NULL"}, allEntries = true)
    void deleteById(@NotNull Integer id);

    @Override
    @CacheEvict(value = {"SFX", "SFX_TRIGGER", "SFX_TRIGGER_LIST", "SFX_TRIGGER_NULL"}, allEntries = true)
    void delete(@NotNull SFXTrigger sfxTrigger);

    @Override
    @CacheEvict(value = {"SFX", "SFX_TRIGGER", "SFX_TRIGGER_LIST", "SFX_TRIGGER_NULL"}, allEntries = true)
    @NotNull <S extends SFXTrigger> S save(@NotNull S sfxTrigger);
}
