package com.marvin.influxdb.configuration;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDbConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDbConfig.class);

  @Bean
  public InfluxDBClient influxDBClient(
      @Value("${influxdb.token}") String token,
      @Value("${influxdb.url}") String url
  ) {
    LOGGER.info("Creating influx db config for: {}", url);
    return InfluxDBClientFactory.create(url, token.toCharArray());
  }

}
