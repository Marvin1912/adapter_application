package com.marvin.itnews.service;

import com.marvin.itnews.dto.ArticleDTO;
import com.marvin.itnews.mapper.ArticleMapper;
import com.marvin.itnews.repository.ArticleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
     * @param category optional category filter
     * @param source   optional source filter
     * @param page     page number
     * @param size     page size
     * @return paginated articles
     */
    public Page<ArticleDTO> getArticles(
            String category, String source, int page, int size
    ) {
        final Pageable pageable = PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "publishedAt")
        );
        return findArticles(category, source, pageable)
                .map(articleMapper::toArticleDTO);
    }

    private org.springframework.data.domain.Page<com.marvin.itnews.entity.Article> findArticles(
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
