package com.marvin.itnews.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "RSS feed source configuration")
public record FeedSourceDTO(
        @Schema(description = "Feed config ID")
        Integer id,

        @Schema(description = "Feed source name", example = "Baeldung")
        String name,

        @Schema(description = "Feed URL")
        String url,

        @Schema(description = "Feed category", example = "Java")
        String category,

        @Schema(description = "Whether the feed is active")
        boolean active
) {

}
