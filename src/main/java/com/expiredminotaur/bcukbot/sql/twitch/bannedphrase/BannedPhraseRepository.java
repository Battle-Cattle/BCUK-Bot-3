package com.expiredminotaur.bcukbot.sql.twitch.bannedphrase;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BannedPhraseRepository extends CrudRepository<BannedPhrase, Integer>
{
    @Override
    @Cacheable(value = "BannedPhrase")
    @NotNull
    List<BannedPhrase> findAll();

    @Override
    @CacheEvict(value = "BannedPhrase", allEntries = true)
    void delete(@NotNull BannedPhrase phase);

    @Override
    @CacheEvict(value = "BannedPhrase", allEntries = true)
    @NotNull <S extends BannedPhrase> S save(@NotNull S phase);
}
