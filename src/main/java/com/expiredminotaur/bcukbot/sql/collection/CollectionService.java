package com.expiredminotaur.bcukbot.sql.collection;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class CollectionService<T>
{
    private final PagingAndSortingRepository<T, Integer> repository;

    public CollectionService(PagingAndSortingRepository<T, Integer> repository)
    {
        this.repository = repository;
    }

    public List<T> findAll()
    {
        return (List<T>) repository.findAll();
    }

    public Stream<T> findAll(int offset, int pageSize)
    {
        return repository.findAll(PageRequest.of(offset / pageSize, pageSize)).stream();
    }

    public long count()
    {
        return repository.count();
    }

    public T save(T data)
    {
        repository.save(data);
        return data;
    }

    public Optional<T> findById(int id)
    {
        return repository.findById(id);
    }
}
