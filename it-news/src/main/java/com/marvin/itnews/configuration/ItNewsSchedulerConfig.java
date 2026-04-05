package com.marvin.itnews.configuration;

import com.marvin.itnews.repository.ArticleRepository;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Configuration
public class ItNewsSchedulerConfig {

    private static final int CONNECT_TIMEOUT_SECONDS = 15;

    private final ArticleRepository articleRepository;

    public ItNewsSchedulerConfig(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Bean
    public HttpClient itNewsHttpClient() {
        return HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onStartup() {
        startCleanUpOldArticles();
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanUpOldArticles() {
        startCleanUpOldArticles();
    }

    private void startCleanUpOldArticles() {
        final LocalDateTime cutoff = LocalDateTime.now().minusWeeks(1);
        log.info("Cleaning up articles older than {}", cutoff);
        final int deleted = articleRepository.deleteArticlesOlderThan(cutoff);
        log.info("Deleted {} old articles", deleted);
    }

}
