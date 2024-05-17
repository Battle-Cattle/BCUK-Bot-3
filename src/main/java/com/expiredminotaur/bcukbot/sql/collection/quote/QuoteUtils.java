package com.expiredminotaur.bcukbot.sql.collection.quote;

import com.expiredminotaur.bcukbot.sql.collection.CollectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class QuoteUtils extends CollectionUtil
{
    @Autowired
    private QuoteRepository quotes;

    @Override
    public String add(String newEntry, String source)
    {
        if (!newEntry.trim().isEmpty())
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
        List<Quote> list = quotes.findAll();
        long qty = list.size();
        if (qty > 0)
        {
            int idx = (int) (Math.random() * qty);
            Quote quote = list.get(idx);
            return String.format("Quote %d: %s [%s]", quote.getId(), quote.getQuote(), quote.getDate().toString());
        }
        return "No quotes in database";
    }
}
