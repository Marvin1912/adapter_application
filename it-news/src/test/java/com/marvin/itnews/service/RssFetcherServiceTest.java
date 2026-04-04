package com.marvin.itnews.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marvin.itnews.entity.Article;
import com.marvin.itnews.entity.FeedConfig;
import com.marvin.itnews.repository.ArticleRepository;
import com.marvin.itnews.repository.FeedConfigRepository;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import java.net.http.HttpClient;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link RssFetcherService} article-age filtering behaviour.
 */
@ExtendWith(MockitoExtension.class)
class RssFetcherServiceTest {

    private static final String FEED_NAME = "Test Feed";
    private static final String FEED_CATEGORY = "tech";
    private static final String ARTICLE_LINK = "https://example.com/article";

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private FeedConfigRepository feedConfigRepository;

    @Mock
    private HttpClient httpClient;

    private RssFetcherService service;
    private FeedConfig feedConfig;

    @BeforeEach
    void setUp() {
        service = new RssFetcherService(articleRepository, feedConfigRepository, httpClient);

        feedConfig = new FeedConfig();
        feedConfig.setName(FEED_NAME);
        feedConfig.setCategory(FEED_CATEGORY);
        feedConfig.setUrl("https://example.com/rss");
        feedConfig.setActive(true);
    }

    @Test
    void shouldSaveArticlePublishedWithinOneMonth() {
        final Date recentDate = toDate(LocalDateTime.now().minusDays(10));
        final SyndFeed feed = buildFeed(List.of(buildEntry(ARTICLE_LINK, recentDate)));

        when(feedConfigRepository.findByActiveTrue()).thenReturn(List.of(feedConfig));
        when(articleRepository.existsByLink(ARTICLE_LINK)).thenReturn(false);

        stubHttpClientToReturn(feed);

        service.fetchAllFeeds();

        final ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
        verify(articleRepository).save(captor.capture());
        assertThat(captor.getValue().getLink()).isEqualTo(ARTICLE_LINK);
    }

    @Test
    void shouldNotSaveArticlePublishedMoreThanOneMonthAgo() {
        final Date oldDate = toDate(LocalDateTime.now().minusMonths(2));
        final SyndFeed feed = buildFeed(List.of(buildEntry(ARTICLE_LINK, oldDate)));

        when(feedConfigRepository.findByActiveTrue()).thenReturn(List.of(feedConfig));
        when(articleRepository.existsByLink(ARTICLE_LINK)).thenReturn(false);

        stubHttpClientToReturn(feed);

        service.fetchAllFeeds();

        verify(articleRepository, never()).save(any());
    }

    @Test
    void shouldNotSaveArticleExactlyOnTheMonthBoundary() {
        final Date borderDate = toDate(LocalDateTime.now().minusMonths(1).minusSeconds(1));
        final SyndFeed feed = buildFeed(List.of(buildEntry(ARTICLE_LINK, borderDate)));

        when(feedConfigRepository.findByActiveTrue()).thenReturn(List.of(feedConfig));
        when(articleRepository.existsByLink(ARTICLE_LINK)).thenReturn(false);

        stubHttpClientToReturn(feed);

        service.fetchAllFeeds();

        verify(articleRepository, never()).save(any());
    }

    @Test
    void shouldNotSaveArticleWithNullLink() {
        final Date recentDate = toDate(LocalDateTime.now().minusDays(1));
        final SyndFeed feed = buildFeed(List.of(buildEntry(null, recentDate)));

        when(feedConfigRepository.findByActiveTrue()).thenReturn(List.of(feedConfig));

        stubHttpClientToReturn(feed);

        service.fetchAllFeeds();

        verify(articleRepository, never()).save(any());
    }

    @Test
    void shouldNotSaveArticleThatAlreadyExists() {
        final Date recentDate = toDate(LocalDateTime.now().minusDays(5));
        final SyndFeed feed = buildFeed(List.of(buildEntry(ARTICLE_LINK, recentDate)));

        when(feedConfigRepository.findByActiveTrue()).thenReturn(List.of(feedConfig));
        when(articleRepository.existsByLink(ARTICLE_LINK)).thenReturn(true);

        stubHttpClientToReturn(feed);

        service.fetchAllFeeds();

        verify(articleRepository, never()).save(any());
    }

    @Test
    void shouldTreatNullPublishedDateAsCurrentTimeAndSave() {
        final SyndFeed feed = buildFeed(List.of(buildEntry(ARTICLE_LINK, null)));

        when(feedConfigRepository.findByActiveTrue()).thenReturn(List.of(feedConfig));
        when(articleRepository.existsByLink(ARTICLE_LINK)).thenReturn(false);

        stubHttpClientToReturn(feed);

        service.fetchAllFeeds();

        verify(articleRepository).save(any());
    }

    // --- helpers ---

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubHttpClientToReturn(SyndFeed feed) {
        try {
            final java.io.InputStream xml = feedToInputStream(feed);
            final java.net.http.HttpResponse response =
                    org.mockito.Mockito.mock(java.net.http.HttpResponse.class);
            when(response.body()).thenReturn(xml);
            when(httpClient.send(any(), any())).thenReturn(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private java.io.InputStream feedToInputStream(SyndFeed feed) throws Exception {
        final com.rometools.rome.io.SyndFeedOutput output =
                new com.rometools.rome.io.SyndFeedOutput();
        final java.io.StringWriter writer = new java.io.StringWriter();
        output.output(feed, writer);
        return new java.io.ByteArrayInputStream(
                writer.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }

    private SyndFeed buildFeed(List<SyndEntry> entries) {
        final SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("rss_2.0");
        feed.setTitle("Test Feed");
        feed.setLink("https://example.com");
        feed.setDescription("Test");
        feed.setEntries(entries);
        return feed;
    }

    private SyndEntry buildEntry(String link, Date publishedDate) {
        final SyndEntry entry = new SyndEntryImpl();
        entry.setTitle("Test Article");
        entry.setLink(link);
        entry.setPublishedDate(publishedDate);
        final SyndContent description = new SyndContentImpl();
        description.setValue("Some description text");
        entry.setDescription(description);
        return entry;
    }

    private Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
