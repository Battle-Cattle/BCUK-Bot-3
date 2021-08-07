package com.expiredminotaur.bcukbot.sql.sfx;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SFXCategoryRepository extends CrudRepository<SFXCategory, Integer>
{
    @Override
    @NotNull List<SFXCategory> findAll();

    @Cacheable(value = "CategoryList")
    SFXCategory getSFXCategoryByNameIgnoreCase(@NotNull String name);

    @Override
    @CacheEvict(value = {"CategoryList"}, allEntries = true)
    @NotNull <S extends SFXCategory> S save(@NotNull S category);
}
