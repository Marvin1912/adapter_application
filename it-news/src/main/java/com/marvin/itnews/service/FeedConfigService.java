package com.marvin.itnews.service;

import com.marvin.itnews.dto.FeedSourceDTO;
import com.marvin.itnews.entity.FeedConfig;
import com.marvin.itnews.mapper.ArticleMapper;
import com.marvin.itnews.repository.FeedConfigRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedConfigService {

    private final FeedConfigRepository feedConfigRepository;
    private final ArticleMapper articleMapper;

    public FeedConfigService(
            FeedConfigRepository feedConfigRepository,
            ArticleMapper articleMapper
    ) {
        this.feedConfigRepository = feedConfigRepository;
        this.articleMapper = articleMapper;
    }

    /**
     * Returns all feed configurations mapped to DTOs.
     *
     * @return list of all feed configs
     */
    public List<FeedSourceDTO> getAllFeedConfigs() {
        return feedConfigRepository.findAll().stream()
                .map(articleMapper::toFeedSourceDTO)
                .toList();
    }

    /**
     * Returns only active feed configurations mapped to DTOs.
     *
     * @return list of active feed configs
     */
    public List<FeedSourceDTO> getActiveFeedConfigs() {
        return feedConfigRepository.findByActiveTrue().stream()
                .map(articleMapper::toFeedSourceDTO)
                .toList();
    }

    /**
     * Creates a new feed configuration.
     *
     * @param dto the feed config data
     * @return the saved feed config as DTO
     */
    @Transactional
    public FeedSourceDTO createFeedConfig(FeedSourceDTO dto) {
        final FeedConfig feedConfig = new FeedConfig();
        feedConfig.setName(dto.name());
        feedConfig.setUrl(dto.url());
        feedConfig.setCategory(dto.category());
        feedConfig.setActive(dto.active());
        final FeedConfig saved = feedConfigRepository.save(feedConfig);
        return articleMapper.toFeedSourceDTO(saved);
    }

    /**
     * Updates an existing feed configuration.
     *
     * @param id  the ID of the feed config to update
     * @param dto the updated feed config data
     * @return the updated feed config as DTO
     * @throws EntityNotFoundException if no feed config exists with the given ID
     */
    @Transactional
    public FeedSourceDTO updateFeedConfig(Integer id, FeedSourceDTO dto) {
        final FeedConfig feedConfig = feedConfigRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FeedConfig not found: " + id));
        feedConfig.setName(dto.name());
        feedConfig.setUrl(dto.url());
        feedConfig.setCategory(dto.category());
        feedConfig.setActive(dto.active());
        final FeedConfig saved = feedConfigRepository.save(feedConfig);
        return articleMapper.toFeedSourceDTO(saved);
    }

    /**
     * Deletes a feed configuration by ID.
     *
     * @param id the ID of the feed config to delete
     * @throws EntityNotFoundException if no feed config exists with the given ID
     */
    @Transactional
    public void deleteFeedConfig(Integer id) {
        if (!feedConfigRepository.existsById(id)) {
            throw new EntityNotFoundException("FeedConfig not found: " + id);
        }
        feedConfigRepository.deleteById(id);
    }

}
