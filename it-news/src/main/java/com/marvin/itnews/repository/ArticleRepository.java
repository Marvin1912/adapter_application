package com.marvin.itnews.repository;

import com.marvin.itnews.entity.Article;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    boolean existsByLink(String link);

    Page<Article> findByCategory(String category, Pageable pageable);

    Page<Article> findBySource(String source, Pageable pageable);

    Page<Article> findByCategoryAndSource(
            String category, String source, Pageable pageable
    );

    Page<Article> findByIsRead(boolean isRead, Pageable pageable);

    Page<Article> findByCategoryAndIsRead(String category, boolean isRead, Pageable pageable);

    Page<Article> findBySourceAndIsRead(String source, boolean isRead, Pageable pageable);

    Page<Article> findByCategoryAndSourceAndIsRead(
            String category, String source, boolean isRead, Pageable pageable
    );

    @Modifying
    @Query("DELETE FROM Article a WHERE a.isRead = true AND a.fetchedAt < :cutoff")
    int deleteReadArticlesOlderThan(LocalDateTime cutoff);

}
