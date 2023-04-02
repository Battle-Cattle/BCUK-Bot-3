package com.expiredminotaur.bcukbot.sql.collection.joke;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface JokeRepository extends CrudRepository<Joke, Integer>
{
    @Override
    @Cacheable(value = "Jokes")
    @NotNull
    List<Joke> findAll();

    @Override
    @CacheEvict(value = "Jokes", allEntries = true)
    void deleteById(@NotNull Integer JokeId);

    @Override
    @CacheEvict(value = "Jokes", allEntries = true)
    void delete(@NotNull Joke Joke);

    @Override
    @CacheEvict(value = "Jokes", allEntries = true)
    @NotNull <S extends Joke> S save(@NotNull S Joke);
}
