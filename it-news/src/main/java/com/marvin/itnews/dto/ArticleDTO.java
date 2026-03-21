package com.marvin.itnews.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Data Transfer Object representing a news article")
public record ArticleDTO(
        @Schema(description = "Unique identifier", example = "1")
        long id,

        @Schema(description = "Article title")
        String title,

        @Schema(description = "Article description or summary")
        String description,

        @Schema(description = "URL to the original article")
        String link,

        @Schema(description = "Feed source name", example = "Baeldung")
        String source,

        @Schema(description = "Article category", example = "Java")
        String category,

        @Schema(description = "When the article was published")
        LocalDateTime publishedAt,

        @Schema(description = "When the article was fetched")
        LocalDateTime fetchedAt
) {

}
