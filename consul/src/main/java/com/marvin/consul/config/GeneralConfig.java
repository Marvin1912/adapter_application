package com.marvin.consul.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marvin.consul.repository.BasicConsulRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeneralConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeneralConfig.class);

  @Bean
  public BasicConsulRepository consulRepository(@Value("${consul.url}") String url,
      ObjectMapper objectMapper) {
    LOGGER.info("Creating basic consul repository for: {}", url);
    return new BasicConsulRepository(url, objectMapper);
  }

}
