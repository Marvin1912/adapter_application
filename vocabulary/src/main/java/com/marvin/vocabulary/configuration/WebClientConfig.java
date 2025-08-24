package com.marvin.vocabulary.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient deeplWebClient() {
        return WebClient.builder()
                .build();
    }

}
