package com.marvin.itnews.repository;

import com.marvin.itnews.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    boolean existsByLink(String link);

    Page<Article> findByCategory(String category, Pageable pageable);

    Page<Article> findBySource(String source, Pageable pageable);

    Page<Article> findByCategoryAndSource(
            String category, String source, Pageable pageable
    );

}
