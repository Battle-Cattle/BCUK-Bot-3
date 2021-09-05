package com.expiredminotaur.bcukbot.sql.user;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long>
{
    @Override
    @Cacheable(value = "Users")
    @NotNull
    List<User> findAll();

    @Override
    @Cacheable(value = "Users")
    @NotNull
    Optional<User> findById(@NotNull Long UserId);


    @Cacheable(value = "BotUsers")
    List<User> findByIsTwitchBotEnabledIsTrue();

    @Override
    @CacheEvict(value = {"Users", "BotUsers"}, allEntries = true)
    void deleteById(@NotNull Long UserId);

    @Override
    @CacheEvict(value = {"Users", "BotUsers"}, allEntries = true)
    void delete(@NotNull User user);

    @Override
    @CacheEvict(value = {"Users", "BotUsers"}, allEntries = true)
    @NotNull <S extends User> S save(@NotNull S user);
}