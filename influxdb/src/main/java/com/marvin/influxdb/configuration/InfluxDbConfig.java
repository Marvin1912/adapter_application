package com.marvin.influxdb.configuration;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for InfluxDB client bean creation.
 *
 * <p>This class provides the necessary configuration to create and configure
 * an InfluxDB client instance for connecting to the InfluxDB database.</p>
 *
 * @author Marvin Application
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class InfluxDbConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDbConfig.class);

    /**
     * Creates and configures an InfluxDB client bean.
     *
     * <p>This method initializes the InfluxDB client using the provided token
     * and URL from the application configuration properties.</p>
     *
     * @param token the authentication token for InfluxDB
     * @param url the URL of the InfluxDB server
     * @return a configured InfluxDB client instance
     */
    @Bean
    public InfluxDBClient influxDBClient(
            @Value("${influxdb.token}") final String token,
            @Value("${influxdb.url}") final String url
    ) {
        LOGGER.info("Creating InfluxDB client for URL: {}", url);
        return InfluxDBClientFactory.create(url, token.toCharArray());
    }
}
