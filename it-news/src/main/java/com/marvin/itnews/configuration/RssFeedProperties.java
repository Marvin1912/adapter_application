package com.marvin.itnews.configuration;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rss")
@Getter
@Setter
public class RssFeedProperties {

    private long pollIntervalMs = 1800000;
    private List<FeedSource> feeds = new ArrayList<>();

    @Getter
    @Setter
    public static class FeedSource {

        private String name;
        private String url;
        private String category;

    }

}
