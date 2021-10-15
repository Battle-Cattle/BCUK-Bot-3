package com.expiredminotaur.bcukbot.sql.collection.quote;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

interface QuoteRepository extends PagingAndSortingRepository<Quote, Integer>
{
    @Override
    //@Cacheable(value = "Quotes")
    @NotNull
    List<Quote> findAll();

    @Override
        //@CacheEvict(value = "Quotes", allEntries = true)
    void deleteById(@NotNull Integer QuoteId);

    @Override
        //@CacheEvict(value = "Quotes", allEntries = true)
    void delete(@NotNull Quote quote);

    @Override
    //@CacheEvict(value = "Quotes", allEntries = true)
    @NotNull <S extends Quote> S save(@NotNull S Quote);
}
