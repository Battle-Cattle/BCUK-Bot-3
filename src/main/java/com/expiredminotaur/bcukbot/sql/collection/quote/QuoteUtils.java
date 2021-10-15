package com.expiredminotaur.bcukbot.sql.collection.quote;

import com.expiredminotaur.bcukbot.sql.collection.CollectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class QuoteUtils extends CollectionUtil
{
    @Autowired
    private QuoteService quotes;

    @Override
    public String add(String newEntry, String source)
    {
        if (newEntry.trim().length() > 0)
        {
            Quote quote = new Quote(newEntry, source);
            quote = quotes.save(quote);
            return String.format("Added Quote %d: %s [%s]", quote.getId(), quote.getQuote(), quote.getDate());
        }
        return "No quote given";
    }

    @Override
    public String get(int id)
    {
        Optional<Quote> oQuote = quotes.findById(id);
        if (oQuote.isPresent())
        {
            Quote quote = oQuote.get();
            return String.format("quote %d: %s [%s]", quote.getId(), quote.getQuote(), quote.getDate().toString());
        }
        throw new IndexOutOfBoundsException();
    }

    public String random()
    {
        long qty = quotes.count();
        if (qty > 0)
        {
            int idx = (int) (Math.random() * qty);
            Quote quote = quotes.findAll(idx, 1).findFirst().get();
            return String.format("Quote %d: %s [%s]", quote.getId(), quote.getQuote(), quote.getDate().toString());
        }
        return "No quotes in database";
    }
}
