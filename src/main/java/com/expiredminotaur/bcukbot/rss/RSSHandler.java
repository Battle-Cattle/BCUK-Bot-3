package com.expiredminotaur.bcukbot.rss;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@Component
public class RSSHandler
{
    private final Logger log = LoggerFactory.getLogger(RSSHandler.class);
    private final String url = "https://www.warhammer-community.com/feed/";

    @Scheduled(cron = "* */5 * * * *")//every 5th minute
    private void checkForUpdates()
    {
        try
        {
            URL feedSource = new URL(url);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedSource));
            List<SyndEntry> newsList = feed.getEntries();
            for (SyndEntry news : newsList)
                postNews(news);

        } catch (FeedException | IOException e)
        {
            log.error("RSS error", e);
        }
    }

    private void postNews(SyndEntry news)
    {
        log.info(news.getPublishedDate().toString());
        log.info(news.getTitle());
        SyndContent description = news.getDescription();
        if (description != null)
        {
            log.info(Jsoup.clean(description.getValue(), Safelist.none()));
        }
        log.info(Jsoup.clean(news.getLink(), Safelist.none()));
        log.info("");
    }
}
