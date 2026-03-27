package com.marvin.itnews.controller;

import com.marvin.itnews.dto.FeedSourceDTO;
import com.marvin.itnews.service.FeedConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping(path = "/it-news/feeds")
@Tag(name = "IT News Feed Config", description = "Endpoints for managing RSS feed configurations")
public class FeedConfigController {

    private final FeedConfigService feedConfigService;

    public FeedConfigController(FeedConfigService feedConfigService) {
        this.feedConfigService = feedConfigService;
    }

    /**
     * Returns all feed configurations, optionally filtered to active only.
     *
     * @param activeOnly when true, only active feeds are returned
     * @return list of feed configs
     */
    @GetMapping("/")
    @Operation(summary = "Get feed configs",
            description = "Returns all RSS feed configurations. "
                    + "Pass activeOnly=true to get only enabled feeds.")
    public Mono<List<FeedSourceDTO>> getFeedConfigs(
            @RequestParam(defaultValue = "false")
            @Parameter(description = "Return only active feeds") boolean activeOnly
    ) {
        return Mono.fromCallable(
                () -> activeOnly
                        ? feedConfigService.getActiveFeedConfigs()
                        : feedConfigService.getAllFeedConfigs()
        ).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Creates a new feed configuration.
     *
     * @param dto the feed config to create
     * @return the created feed config
     */
    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create feed config",
            description = "Creates a new RSS feed configuration.")
    public Mono<FeedSourceDTO> createFeedConfig(
            @Valid @RequestBody FeedSourceDTO dto
    ) {
        return Mono.fromCallable(() -> feedConfigService.createFeedConfig(dto))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Updates an existing feed configuration.
     *
     * @param id  the ID of the feed config to update
     * @param dto the updated feed config data
     * @return the updated feed config
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update feed config",
            description = "Updates an existing RSS feed configuration.")
    public Mono<FeedSourceDTO> updateFeedConfig(
            @PathVariable @Parameter(description = "Feed config ID") Integer id,
            @Valid @RequestBody FeedSourceDTO dto
    ) {
        return Mono.fromCallable(() -> feedConfigService.updateFeedConfig(id, dto))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Deletes a feed configuration by ID.
     *
     * @param id the ID of the feed config to delete
     * @return empty response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete feed config",
            description = "Deletes an RSS feed configuration by ID.")
    public Mono<ResponseEntity<Void>> deleteFeedConfig(
            @PathVariable @Parameter(description = "Feed config ID") Integer id
    ) {
        return Mono.fromCallable(() -> {
            feedConfigService.deleteFeedConfig(id);
            return ResponseEntity.ok().<Void>build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

}
