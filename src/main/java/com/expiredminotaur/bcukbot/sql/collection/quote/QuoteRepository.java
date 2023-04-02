package com.expiredminotaur.bcukbot.sql.collection.quote;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QuoteRepository extends CrudRepository<Quote, Integer>
{
    @Override
    @Cacheable(value = "Quotes")
    @NotNull
    List<Quote> findAll();

    @Override
    @CacheEvict(value = "Quotes", allEntries = true)
    void deleteById(@NotNull Integer QuoteId);

    @Override
    @CacheEvict(value = "Quotes", allEntries = true)
    void delete(@NotNull Quote quote);

    @Override
    @CacheEvict(value = "Quotes", allEntries = true)
    @NotNull <S extends Quote> S save(@NotNull S Quote);
}
