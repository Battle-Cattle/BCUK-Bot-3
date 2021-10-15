package com.expiredminotaur.bcukbot.sql.collection.clip;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

interface ClipRepository extends PagingAndSortingRepository<Clip, Integer>
{
    @Override
    //@Cacheable(value = "Clips")
    @NotNull
    List<Clip> findAll();

    @Override
        //@CacheEvict(value = "Clips", allEntries = true)
    void deleteById(@NotNull Integer ClipId);

    @Override
        //@CacheEvict(value = "Clips", allEntries = true)
    void delete(@NotNull Clip Clip);

    @Override
    //@CacheEvict(value = "Clips", allEntries = true)
    @NotNull <S extends Clip> S save(@NotNull S Clip);
}