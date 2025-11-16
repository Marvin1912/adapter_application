package com.marvin.app.controller.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InfluxBucketResponse Tests")
class InfluxBucketResponseTest {

    private List<InfluxBucketResponse.InfluxBucketDTO> testBuckets;

    @BeforeEach
    void setUp() {
        testBuckets = List.of(
                new InfluxBucketResponse.InfluxBucketDTO("SYSTEM_METRICS", "system_metrics", "System metrics"),
                new InfluxBucketResponse.InfluxBucketDTO("TEMPERATURE", "sensor_data", "Temperature data")
        );
    }

    @Test
    @DisplayName("Should create successful response with buckets")
    void success_ValidBuckets() {
        // Act
        InfluxBucketResponse response = InfluxBucketResponse.success(testBuckets);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBuckets());
        assertEquals(2, response.getBuckets().size());
        assertEquals("Available InfluxDB buckets retrieved successfully", response.getMessage());
        assertTrue(response.getTimestamp() > 0);

        // Verify bucket data
        InfluxBucketResponse.InfluxBucketDTO firstBucket = response.getBuckets().get(0);
        assertEquals("SYSTEM_METRICS", firstBucket.getName());
        assertEquals("system_metrics", firstBucket.getBucketName());
        assertEquals("System metrics", firstBucket.getDescription());
    }

    @Test
    @DisplayName("Should create error response with message")
    void error_ValidErrorMessage() {
        // Act
        InfluxBucketResponse response = InfluxBucketResponse.error("Test error message");

        // Assert
        assertNotNull(response);
        assertNull(response.getBuckets());
        assertEquals("Test error message", response.getMessage());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    @DisplayName("Should create successful response with empty bucket list")
    void success_EmptyBucketList() {
        // Act
        InfluxBucketResponse response = InfluxBucketResponse.success(List.of());

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBuckets());
        assertEquals(0, response.getBuckets().size());
        assertEquals("Available InfluxDB buckets retrieved successfully", response.getMessage());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    @DisplayName("Should verify InfluxBucketDTO getters and setters work correctly")
    void influxBucketDTO_GettersSetters() {
        // Arrange
        InfluxBucketResponse.InfluxBucketDTO dto = new InfluxBucketResponse.InfluxBucketDTO("TEST", "test_bucket", "Test description");

        // Act & Assert - Test getters
        assertEquals("TEST", dto.getName());
        assertEquals("test_bucket", dto.getBucketName());
        assertEquals("Test description", dto.getDescription());

        // Test setters
        dto.setName("NEW_TEST");
        dto.setBucketName("new_bucket");
        dto.setDescription("New description");

        assertEquals("NEW_TEST", dto.getName());
        assertEquals("new_bucket", dto.getBucketName());
        assertEquals("New description", dto.getDescription());
    }

    @Test
    @DisplayName("Should create multiple valid InfluxBucketDTO instances")
    void influxBucketDTO_MultipleInstances() {
        // Arrange & Act
        InfluxBucketResponse.InfluxBucketDTO systemMetrics = new InfluxBucketResponse.InfluxBucketDTO(
                "SYSTEM_METRICS", "system_metrics", "System performance metrics");
        InfluxBucketResponse.InfluxBucketDTO temperature = new InfluxBucketResponse.InfluxBucketDTO(
                "TEMPERATURE", "sensor_data", "Temperature sensor data");
        InfluxBucketResponse.InfluxBucketDTO humidity = new InfluxBucketResponse.InfluxBucketDTO(
                "HUMIDITY", "sensor_data", "Humidity sensor data");
        InfluxBucketResponse.InfluxBucketDTO temperatureAggregated = new InfluxBucketResponse.InfluxBucketDTO(
                "TEMPERATURE_AGGREGATED", "sensor_data_30m", "30-minute per hour aggregated temperature data");
        InfluxBucketResponse.InfluxBucketDTO humidityAggregated = new InfluxBucketResponse.InfluxBucketDTO(
                "HUMIDITY_AGGREGATED", "sensor_data_30m", "30-minute per hour aggregated humidity data");

        // Assert
        assertEquals("SYSTEM_METRICS", systemMetrics.getName());
        assertEquals("system_metrics", systemMetrics.getBucketName());
        assertEquals("System performance metrics", systemMetrics.getDescription());

        assertEquals("TEMPERATURE", temperature.getName());
        assertEquals("sensor_data", temperature.getBucketName());
        assertEquals("Temperature sensor data", temperature.getDescription());

        assertEquals("HUMIDITY", humidity.getName());
        assertEquals("sensor_data", humidity.getBucketName());
        assertEquals("Humidity sensor data", humidity.getDescription());

        assertEquals("TEMPERATURE_AGGREGATED", temperatureAggregated.getName());
        assertEquals("sensor_data_30m", temperatureAggregated.getBucketName());
        assertEquals("30-minute per hour aggregated temperature data", temperatureAggregated.getDescription());

        assertEquals("HUMIDITY_AGGREGATED", humidityAggregated.getName());
        assertEquals("sensor_data_30m", humidityAggregated.getBucketName());
        assertEquals("30-minute per hour aggregated humidity data", humidityAggregated.getDescription());
    }

    @Test
    @DisplayName("Should verify timestamp is set correctly")
    void responseTimestamp_SetCorrectly() {
        // Arrange
        long beforeCreation = System.currentTimeMillis();

        // Act
        InfluxBucketResponse successResponse = InfluxBucketResponse.success(testBuckets);
        InfluxBucketResponse errorResponse = InfluxBucketResponse.error("Error message");

        // Assert
        long afterCreation = System.currentTimeMillis();

        assertTrue(successResponse.getTimestamp() >= beforeCreation);
        assertTrue(successResponse.getTimestamp() <= afterCreation);
        assertTrue(errorResponse.getTimestamp() >= beforeCreation);
        assertTrue(errorResponse.getTimestamp() <= afterCreation);
    }
}