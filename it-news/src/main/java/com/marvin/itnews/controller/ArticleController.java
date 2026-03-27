package com.marvin.itnews.controller;

import com.marvin.itnews.dto.ArticleDTO;
import com.marvin.itnews.dto.FeedSourceDTO;
import com.marvin.itnews.service.ArticleService;
import com.marvin.itnews.service.FeedConfigService;
import com.marvin.itnews.service.RssFetcherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping(path = "/it-news")
@Tag(name = "IT News", description = "Endpoints for browsing aggregated IT news articles")
public class ArticleController {

    private final ArticleService articleService;
    private final FeedConfigService feedConfigService;
    private final RssFetcherService rssFetcherService;

    public ArticleController(
            ArticleService articleService,
            FeedConfigService feedConfigService,
            RssFetcherService rssFetcherService
    ) {
        this.articleService = articleService;
        this.feedConfigService = feedConfigService;
        this.rssFetcherService = rssFetcherService;
    }

    /**
     * Retrieves a paginated list of articles with optional filters.
     *
     * @param category    optional category filter
     * @param source      optional source filter
     * @param includeRead whether to include read articles
     * @param page        page number
     * @param size        page size
     * @return paginated articles
     */
    @GetMapping("/articles")
    @Operation(summary = "Get articles",
            description = "Retrieves a paginated list of news articles, "
                    + "optionally filtered by category and/or source.")
    public Mono<Page<ArticleDTO>> getArticles(
            @RequestParam(required = false)
            @Parameter(description = "Filter by category") String category,
            @RequestParam(required = false)
            @Parameter(description = "Filter by source") String source,
            @RequestParam(defaultValue = "false")
            @Parameter(description = "Include read articles") boolean includeRead,
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "20")
            @Parameter(description = "Page size") int size
    ) {
        return Mono.fromCallable(
                () -> articleService.getArticles(category, source, includeRead, page, size)
        ).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Returns the list of active RSS feed sources.
     *
     * @return list of active feed sources
     */
    @GetMapping("/sources")
    @Operation(summary = "Get feed sources",
            description = "Returns all active RSS feed sources.")
    public Mono<List<FeedSourceDTO>> getSources() {
        return Mono.fromCallable(feedConfigService::getActiveFeedConfigs)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Marks an article as read.
     *
     * @param id article ID
     * @return empty response
     */
    @PatchMapping("/articles/{id}/read")
    @Operation(summary = "Mark article as read",
            description = "Marks the specified article as read.")
    public Mono<ResponseEntity<Void>> markAsRead(
            @PathVariable @Parameter(description = "Article ID") long id
    ) {
        return Mono.fromCallable(() -> {
            articleService.markAsRead(id);
            return ResponseEntity.ok().<Void>build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Triggers an immediate fetch of all RSS feeds.
     *
     * @return confirmation message
     */
    @PostMapping("/fetch")
    @Operation(summary = "Trigger feed fetch",
            description = "Manually triggers fetching of all RSS feeds.")
    public Mono<ResponseEntity<String>> triggerFetch() {
        return Mono.fromCallable(() -> {
            rssFetcherService.fetchAllFeeds();
            return ResponseEntity.ok("Feed fetch triggered");
        }).subscribeOn(Schedulers.boundedElastic());
    }

}
