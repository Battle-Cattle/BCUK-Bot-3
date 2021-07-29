package com.expiredminotaur.bcukbot.sql.twitch.streams.streamer;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StreamerRepository extends JpaRepository<Streamer, Integer>
{
    @Override
    @Cacheable(value = "Streams")
    @NotNull
    List<Streamer> findAll();

    @Override
    @CacheEvict(value = "Streams", allEntries = true)
    void deleteById(@NotNull Integer StreamerId);

    @Override
    @CacheEvict(value = "Streams", allEntries = true)
    void delete(@NotNull Streamer Streamer);

    @Override
    @CacheEvict(value = "Streams", allEntries = true)
    @NotNull <S extends Streamer> S save(@NotNull S Streamer);
}
