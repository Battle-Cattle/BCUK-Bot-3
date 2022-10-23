package com.expiredminotaur.bcukbot.sql.counter;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CounterRepository extends CrudRepository<Counter, Integer>
{
    @Override
    @Cacheable(value = "Counters")
    @NotNull
    List<Counter> findAll();

    @Query("from Counter where lower(triggerCommand)=:message")
    List<Counter> findByTrigger(@Param("message") String message);

    @Query("from Counter where lower(checkCommand)=:message")
    List<Counter> findByCheck(@Param("message") String message);

    @Override
    @CacheEvict(value = "Counters", allEntries = true)
    void deleteById(@NotNull Integer CounterId);

    @Override
    @CacheEvict(value = "Counters", allEntries = true)
    void delete(@NotNull Counter Counter);

    @Override
    @CacheEvict(value = "Counters", allEntries = true)
    @NotNull <S extends Counter> S save(@NotNull S Counter);
}
