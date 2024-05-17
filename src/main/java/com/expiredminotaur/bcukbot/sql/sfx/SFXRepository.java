package com.expiredminotaur.bcukbot.sql.sfx;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SFXRepository extends CrudRepository<SFX, Integer>
{
    @Override
    @Cacheable(value = "SFX")
    @NotNull
    List<SFX> findAll();

    @Cacheable(value = "SFX")
    List<SFX> getByTriggerId(Long id);

    @Override
    @CacheEvict(value = {"SFX", "SFX_TRIGGER", "SFX_TRIGGER_LIST", "SFX_TRIGGER_NULL"}, allEntries = true)
    void deleteById(@NotNull Integer id);

    @Override
    @CacheEvict(value = {"SFX", "SFX_TRIGGER", "SFX_TRIGGER_LIST", "SFX_TRIGGER_NULL"}, allEntries = true)
    void delete(@NotNull SFX sfx);

    @Override
    @CacheEvict(value = {"SFX", "SFX_TRIGGER", "SFX_TRIGGER_LIST", "SFX_TRIGGER_NULL"}, allEntries = true)
    @NotNull
    <S extends SFX> S save(@NotNull S sfx);

}
