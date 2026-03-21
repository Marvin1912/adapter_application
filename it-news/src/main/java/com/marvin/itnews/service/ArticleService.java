package com.marvin.itnews.service;

import com.marvin.itnews.dto.ArticleDTO;
import com.marvin.itnews.mapper.ArticleMapper;
import com.marvin.itnews.repository.ArticleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;

    public ArticleService(
            ArticleRepository articleRepository,
            ArticleMapper articleMapper
    ) {
        this.articleRepository = articleRepository;
        this.articleMapper = articleMapper;
    }

    /**
     * Retrieves articles with optional filtering by category and source.
     *
     * @param category    optional category filter
     * @param source      optional source filter
     * @param includeRead whether to include read articles
     * @param page        page number
     * @param size        page size
     * @return paginated articles
     */
    public Page<ArticleDTO> getArticles(
            String category, String source, boolean includeRead, int page, int size
    ) {
        final Pageable pageable = PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "publishedAt")
        );
        return findArticles(category, source, includeRead, pageable)
                .map(articleMapper::toArticleDTO);
    }

    @Transactional
    public void markAsRead(long id) {
        final var article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article not found: " + id));
        article.setRead(true);
        articleRepository.save(article);
    }

    private Page<com.marvin.itnews.entity.Article> findArticles(
            String category, String source, boolean includeRead, Pageable pageable
    ) {
        if (includeRead) {
            return findAllArticles(category, source, pageable);
        }
        if (category != null && source != null) {
            return articleRepository
                    .findByCategoryAndSourceAndIsRead(category, source, false, pageable);
        } else if (category != null) {
            return articleRepository
                    .findByCategoryAndIsRead(category, false, pageable);
        } else if (source != null) {
            return articleRepository
                    .findBySourceAndIsRead(source, false, pageable);
        }
        return articleRepository.findByIsRead(false, pageable);
    }

    private Page<com.marvin.itnews.entity.Article> findAllArticles(
            String category, String source, Pageable pageable
    ) {
        if (category != null && source != null) {
            return articleRepository
                    .findByCategoryAndSource(category, source, pageable);
        } else if (category != null) {
            return articleRepository
                    .findByCategory(category, pageable);
        } else if (source != null) {
            return articleRepository
                    .findBySource(source, pageable);
        }
        return articleRepository.findAll(pageable);
    }

}
