package com.marvin.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.marvin.app.controller.dto.InfluxBucketResponse;
import com.marvin.export.InfluxExporter;
import com.marvin.upload.Uploader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
        influxExportController = new InfluxExportController(mock(InfluxExporter.class), mock(Uploader.class));
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
        assertFalse(buckets.isEmpty());

        // Verify that response contains same number of buckets as enum
        int expectedBucketCount = InfluxExporter.InfluxBucket.values().length;
        assertEquals(expectedBucketCount, buckets.size());

        // Verify all bucket names are unique and correspond to enum values
        Set<String> responseBucketNames = buckets.stream()
                .map(InfluxBucketResponse.InfluxBucketDTO::getName)
                .collect(Collectors.toSet());

        Set<String> expectedBucketNames = Arrays.stream(InfluxExporter.InfluxBucket.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        assertEquals(expectedBucketNames, responseBucketNames);
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

    @Test
    @DisplayName("Should verify bucket data matches enum values")
    void getAvailableBuckets_VerifyDataIntegrity() {
        // Act
        ResponseEntity<InfluxBucketResponse> response = influxExportController.getAvailableBuckets();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<InfluxBucketResponse.InfluxBucketDTO> buckets = response.getBody().getBuckets();

        // Verify each bucket DTO has matching enum data
        for (InfluxExporter.InfluxBucket enumBucket : InfluxExporter.InfluxBucket.values()) {
            boolean bucketFound = buckets.stream()
                    .anyMatch(dto ->
                        enumBucket.name().equals(dto.getName()) &&
                        enumBucket.getBucketName().equals(dto.getBucketName()) &&
                        enumBucket.getDescription().equals(dto.getDescription())
                    );

            assertTrue(bucketFound, "Bucket " + enumBucket.name() + " should be present with correct data");
        }
    }
}
