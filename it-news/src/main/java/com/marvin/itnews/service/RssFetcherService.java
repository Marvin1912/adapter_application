package com.marvin.itnews.service;

import com.marvin.itnews.entity.Article;
import com.marvin.itnews.entity.FeedConfig;
import com.marvin.itnews.repository.ArticleRepository;
import com.marvin.itnews.repository.FeedConfigRepository;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.InputSource;

@Service
@Slf4j
public class RssFetcherService {

    private static final int REQUEST_TIMEOUT_SECONDS = 30;
    private static final int MAX_DESCRIPTION_LENGTH = 4000;

    private final ArticleRepository articleRepository;
    private final FeedConfigRepository feedConfigRepository;
    private final HttpClient httpClient;

    public RssFetcherService(
        ArticleRepository articleRepository,
        FeedConfigRepository feedConfigRepository,
        HttpClient itNewsHttpClient
    ) {
        this.articleRepository = articleRepository;
        this.feedConfigRepository = feedConfigRepository;
        this.httpClient = itNewsHttpClient;
    }

    /**
     * Fetches all active RSS feeds on a scheduled interval.
     */
    @Scheduled(fixedDelayString = "${rss.poll-interval-ms:1800000}")
    @Transactional
    public void fetchAllFeeds() {
        final List<FeedConfig> activeFeeds = feedConfigRepository.findByActiveTrue();
        log.info("Starting RSS feed fetch for {} sources", activeFeeds.size());
        for (FeedConfig feedConfig : activeFeeds) {
            fetchFeed(feedConfig);
        }
        log.info("RSS feed fetch completed");
    }

    private void fetchFeed(FeedConfig feedConfig) {
        try {
            final SyndFeed feed = downloadFeed(feedConfig.getUrl());
            final int newArticles = processFeedEntries(feed, feedConfig);
            log.info("Fetched {} new articles from {}",
                newArticles, feedConfig.getName());
        } catch (Exception e) {
            log.error("Failed to fetch RSS feed from {}: {}",
                feedConfig.getName(), e.getMessage());
        }
    }

    private SyndFeed downloadFeed(String url) throws Exception {
        final HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .header("User-Agent", "IT-News-Aggregator/1.0")
            .build();
        final HttpResponse<InputStream> response = httpClient.send(
            request, HttpResponse.BodyHandlers.ofInputStream()
        );
        return new SyndFeedInput().build(new InputSource(response.body()));
    }

    private int processFeedEntries(SyndFeed feed, FeedConfig feedConfig) {
        int newArticles = 0;
        for (SyndEntry entry : feed.getEntries()) {
            if (saveEntryIfNew(entry, feedConfig)) {
                newArticles++;
            }
        }
        return newArticles;
    }

    private boolean saveEntryIfNew(SyndEntry entry, FeedConfig feedConfig) {
        final String link = entry.getLink();
        if (link == null || articleRepository.existsByLink(link)) {
            return false;
        }
        final LocalDateTime publishedAt = toLocalDateTime(entry.getPublishedDate());
        if (publishedAt.isBefore(LocalDateTime.now().minusMonths(1))) {
            return false;
        }
        final Article article = buildArticle(entry, feedConfig);
        articleRepository.save(article);
        return true;
    }

    private Article buildArticle(SyndEntry entry, FeedConfig feedConfig) {
        final Article article = new Article();
        article.setTitle(entry.getTitle());
        article.setDescription(extractDescription(entry));
        article.setLink(entry.getLink());
        article.setSource(feedConfig.getName());
        article.setCategory(feedConfig.getCategory());
        article.setPublishedAt(toLocalDateTime(entry.getPublishedDate()));
        article.setFetchedAt(LocalDateTime.now());
        return article;
    }

    private String extractDescription(SyndEntry entry) {
        if (entry.getDescription() == null) {
            return null;
        }
        return stripAndTruncate(entry.getDescription().getValue());
    }

    private String stripAndTruncate(String text) {
        if (text == null) {
            return null;
        }
        final String clean = text.replaceAll("<[^>]*>", "").trim();
        if (clean.length() > MAX_DESCRIPTION_LENGTH) {
            return clean.substring(0, MAX_DESCRIPTION_LENGTH);
        }
        return clean;
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return LocalDateTime.now();
        }
        return date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    }

}
