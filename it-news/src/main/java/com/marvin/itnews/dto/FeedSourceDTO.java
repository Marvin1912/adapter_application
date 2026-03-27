package com.marvin.itnews.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "RSS feed source configuration")
public record FeedSourceDTO(
        @Schema(description = "Feed config ID")
        Integer id,

        @NotBlank
        @Schema(description = "Feed source name", example = "Baeldung")
        String name,

        @NotBlank
        @Schema(description = "Feed URL")
        String url,

        @NotBlank
        @Schema(description = "Feed category", example = "Java")
        String category,

        @Schema(description = "Whether the feed is active")
        boolean active
) {

}
