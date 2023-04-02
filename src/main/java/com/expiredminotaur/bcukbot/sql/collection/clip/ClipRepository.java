package com.expiredminotaur.bcukbot.sql.collection.clip;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ClipRepository extends CrudRepository<Clip, Integer>
{
    @Override
    @Cacheable(value = "Clips")
    @NotNull
    List<Clip> findAll();

    @Override
    @CacheEvict(value = "Clips", allEntries = true)
    void deleteById(@NotNull Integer ClipId);

    @Override
    @CacheEvict(value = "Clips", allEntries = true)
    void delete(@NotNull Clip Clip);

    @Override
    @CacheEvict(value = "Clips", allEntries = true)
    @NotNull <S extends Clip> S save(@NotNull S Clip);
}