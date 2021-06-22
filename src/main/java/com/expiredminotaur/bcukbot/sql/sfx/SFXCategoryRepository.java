package com.expiredminotaur.bcukbot.sql.sfx;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SFXCategoryRepository extends CrudRepository<SFXCategory, Integer>
{
    @Override
    List<SFXCategory> findAll();
}
