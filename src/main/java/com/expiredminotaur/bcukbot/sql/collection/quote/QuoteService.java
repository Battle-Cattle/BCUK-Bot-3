package com.expiredminotaur.bcukbot.sql.collection.quote;

import com.expiredminotaur.bcukbot.sql.collection.CollectionService;
import org.springframework.stereotype.Service;

@Service
public class QuoteService extends CollectionService<Quote>
{
    public QuoteService(QuoteRepository repository)
    {
        super(repository);
    }
}
