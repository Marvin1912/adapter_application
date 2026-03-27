package com.marvin.itnews.configuration;

import com.marvin.itnews.repository.ArticleRepository;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Configuration
public class SchedulerConfig {

    private final ArticleRepository articleRepository;

    public SchedulerConfig(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanUpOldReadArticles() {
        final LocalDateTime cutoff = LocalDateTime.now().minusMonths(1);
        log.info("Cleaning up read articles older than {}", cutoff);
        final int deleted = articleRepository.deleteReadArticlesOlderThan(cutoff);
        log.info("Deleted {} old read articles", deleted);
    }

}
