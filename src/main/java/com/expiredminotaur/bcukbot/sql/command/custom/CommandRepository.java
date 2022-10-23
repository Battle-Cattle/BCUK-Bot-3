package com.expiredminotaur.bcukbot.sql.command.custom;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommandRepository extends CrudRepository<CustomCommand, Integer>
{
    @Override
    @Cacheable(value = "CustomCommand")
    @NotNull
    List<CustomCommand> findAll();

    @Query("from CustomCommand where isDiscordEnabled=true and lower(triggerString)=:trigger")
    @Cacheable(value = "CustomCommand")
    CustomCommand findDiscord(@Param("trigger") String trigger);

    @Query("from CustomCommand c, in (c.twitchEnabledUsers) u where lower(u.twitchName)=:channel and c.triggerString=:trigger")
    @Cacheable(value = "CustomCommand")
    CustomCommand findTwitch(@Param("channel") String channel, @Param("trigger") String trigger);

    @Override
    @CacheEvict(value = "CustomCommand", allEntries = true)
    void deleteById(@NotNull Integer commandId);

    @Override
    @CacheEvict(value = "CustomCommand", allEntries = true)
    void delete(@NotNull CustomCommand command);

    @Override
    @CacheEvict(value = "CustomCommand", allEntries = true)
    @NotNull <S extends CustomCommand> S save(@NotNull S command);
}
