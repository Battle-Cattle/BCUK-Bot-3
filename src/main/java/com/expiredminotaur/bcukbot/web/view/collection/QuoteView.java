package com.expiredminotaur.bcukbot.web.view.collection;

import com.expiredminotaur.bcukbot.sql.collection.quote.Quote;
import com.expiredminotaur.bcukbot.sql.collection.quote.QuoteService;
import com.expiredminotaur.bcukbot.web.layout.MainLayout;
import com.expiredminotaur.bcukbot.web.security.AccessLevel;
import com.expiredminotaur.bcukbot.web.security.UserTools;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@AccessLevel
@Route(value = "quotes", layout = MainLayout.class)
public class QuoteView extends CollectionView<Quote>
{
    public QuoteView(@Autowired UserTools userTools, @Autowired QuoteService service)
    {
        super(userTools, service, Quote.class);
        setup("Quotes", "quote", "Quote");
    }
}