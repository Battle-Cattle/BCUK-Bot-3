package com.expiredminotaur.bcukbot.sql.twitch.streams.group;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Integer>
{
    @Override
    @Cacheable(value = "Streams")
    @NotNull
    List<Group> findAll();

    @Override
    @CacheEvict(value = "Streams", allEntries = true)
    void deleteById(@NotNull Integer GroupId);

    @Override
    @CacheEvict(value = "Streams", allEntries = true)
    void delete(@NotNull Group Group);

    @Override
    @CacheEvict(value = "Streams", allEntries = true)
    @NotNull <S extends Group> S save(@NotNull S Group);
}
