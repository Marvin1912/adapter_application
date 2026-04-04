package com.marvin.itnews.configuration;

import com.marvin.itnews.repository.ArticleRepository;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Configuration
public class ItNewsSchedulerConfig {

    private final ArticleRepository articleRepository;

    public ItNewsSchedulerConfig(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
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
        final LocalDateTime cutoff = LocalDateTime.now().minusMonths(1);
        log.info("Cleaning up articles older than {}", cutoff);
        final int deleted = articleRepository.deleteArticlesOlderThan(cutoff);
        log.info("Deleted {} old articles", deleted);
    }

}
