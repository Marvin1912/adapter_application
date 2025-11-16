package com.marvin.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.marvin.app.controller.dto.InfluxBucketResponse;
import com.marvin.export.InfluxExporter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@DisplayName("InfluxExportController Tests")
class InfluxExportControllerTest {

    @InjectMocks
    private InfluxExportController influxExportController;

    @BeforeEach
    void setUp() {
        influxExportController = new InfluxExportController(mock(InfluxExporter.class));
    }

    @Test
    @DisplayName("Should return all available InfluxDB buckets successfully")
    void getAvailableBuckets_Success() {
        // Act
        ResponseEntity<InfluxBucketResponse> response = influxExportController.getAvailableBuckets();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        List<InfluxBucketResponse.InfluxBucketDTO> buckets = response.getBody().getBuckets();
        assertNotNull(buckets);
        assertEquals(5, buckets.size());

        // Verify each bucket has correct information
        InfluxBucketResponse.InfluxBucketDTO systemMetrics = buckets.stream()
                .filter(bucket -> "SYSTEM_METRICS".equals(bucket.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(systemMetrics);
        assertEquals("system_metrics", systemMetrics.getBucketName());
        assertEquals("System performance metrics (CPU, memory, disk, network)", systemMetrics.getDescription());

        InfluxBucketResponse.InfluxBucketDTO temperature = buckets.stream()
                .filter(bucket -> "TEMPERATURE".equals(bucket.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(temperature);
        assertEquals("sensor_data", temperature.getBucketName());
        assertEquals("Temperature sensor data", temperature.getDescription());

        InfluxBucketResponse.InfluxBucketDTO humidity = buckets.stream()
                .filter(bucket -> "HUMIDITY".equals(bucket.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(humidity);
        assertEquals("sensor_data", humidity.getBucketName());
        assertEquals("Humidity sensor data", humidity.getDescription());

        InfluxBucketResponse.InfluxBucketDTO temperatureAggregated = buckets.stream()
                .filter(bucket -> "TEMPERATURE_AGGREGATED".equals(bucket.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(temperatureAggregated);
        assertEquals("sensor_data_30m", temperatureAggregated.getBucketName());
        assertEquals("30-minute per hour aggregated temperature data", temperatureAggregated.getDescription());

        InfluxBucketResponse.InfluxBucketDTO humidityAggregated = buckets.stream()
                .filter(bucket -> "HUMIDITY_AGGREGATED".equals(bucket.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(humidityAggregated);
        assertEquals("sensor_data_30m", humidityAggregated.getBucketName());
        assertEquals("30-minute per hour aggregated humidity data", humidityAggregated.getDescription());
    }

    @Test
    @DisplayName("Should return success message in response")
    void getAvailableBuckets_SuccessMessage() {
        // Act
        ResponseEntity<InfluxBucketResponse> response = influxExportController.getAvailableBuckets();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Available InfluxDB buckets retrieved successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertTrue(response.getBody().getTimestamp() > 0);
    }

    @Test
    @DisplayName("Should verify bucket DTO structure is correct")
    void getAvailableBuckets_VerifyStructure() {
        // Act
        ResponseEntity<InfluxBucketResponse> response = influxExportController.getAvailableBuckets();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<InfluxBucketResponse.InfluxBucketDTO> buckets = response.getBody().getBuckets();

        for (InfluxBucketResponse.InfluxBucketDTO bucket : buckets) {
            assertNotNull(bucket.getName());
            assertNotNull(bucket.getBucketName());
            assertNotNull(bucket.getDescription());
            assertFalse(bucket.getName().isEmpty());
            assertFalse(bucket.getBucketName().isEmpty());
            assertFalse(bucket.getDescription().isEmpty());
        }
    }
}