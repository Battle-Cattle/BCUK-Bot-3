package com.expiredminotaur.bcukbot.sql.command.alias;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AliasRepository extends CrudRepository<Alias, Integer>
{
    @Override
    @Cacheable(value = "Alias")
    @NotNull
    List<Alias> findAll();

    @Query("from Alias where lower(shortCommand)=:command")
    @Cacheable(value = "Alias")
    List<Alias> findByTrigger(@Param("command") String command);

    @Override
    @CacheEvict(value = "Alias", allEntries = true)
    void deleteById(@NotNull Integer id);

    @Override
    @CacheEvict(value = "Alias", allEntries = true)
    void delete(@NotNull Alias alias);

    @Override
    @CacheEvict(value = "Alias", allEntries = true)
    @NotNull <S extends Alias> S save(@NotNull S alias);
}
