package com.marvin.app.controller.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
                new InfluxBucketResponse.InfluxBucketDTO("TEST_BUCKET_1", "test_bucket_1", "Test description 1"),
                new InfluxBucketResponse.InfluxBucketDTO("TEST_BUCKET_2", "test_bucket_2", "Test description 2")
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
        assertEquals(testBuckets.size(), response.getBuckets().size());
        assertEquals("Available InfluxDB buckets retrieved successfully", response.getMessage());
        assertTrue(response.getTimestamp() > 0);
        assertTrue(response.isSuccess());

        // Verify bucket data is preserved correctly
        for (int i = 0; i < testBuckets.size(); i++) {
            InfluxBucketResponse.InfluxBucketDTO expected = testBuckets.get(i);
            InfluxBucketResponse.InfluxBucketDTO actual = response.getBuckets().get(i);

            assertEquals(expected.getName(), actual.getName());
            assertEquals(expected.getBucketName(), actual.getBucketName());
            assertEquals(expected.getDescription(), actual.getDescription());
        }
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
        assertFalse(response.isSuccess());
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
        assertTrue(response.isSuccess());
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
    @DisplayName("Should create multiple InfluxBucketDTO instances with different data")
    void influxBucketDTO_MultipleInstances() {
        // Arrange & Act
        InfluxBucketResponse.InfluxBucketDTO bucket1 = new InfluxBucketResponse.InfluxBucketDTO(
                "BUCKET_ONE", "bucket_one", "First bucket description");
        InfluxBucketResponse.InfluxBucketDTO bucket2 = new InfluxBucketResponse.InfluxBucketDTO(
                "BUCKET_TWO", "bucket_two", "Second bucket description");
        InfluxBucketResponse.InfluxBucketDTO bucket3 = new InfluxBucketResponse.InfluxBucketDTO(
                "BUCKET_THREE", "bucket_three", "Third bucket description");

        // Assert - Verify each bucket maintains its own data
        assertEquals("BUCKET_ONE", bucket1.getName());
        assertEquals("bucket_one", bucket1.getBucketName());
        assertEquals("First bucket description", bucket1.getDescription());

        assertEquals("BUCKET_TWO", bucket2.getName());
        assertEquals("bucket_two", bucket2.getBucketName());
        assertEquals("Second bucket description", bucket2.getDescription());

        assertEquals("BUCKET_THREE", bucket3.getName());
        assertEquals("bucket_three", bucket3.getBucketName());
        assertEquals("Third bucket description", bucket3.getDescription());

        // Verify buckets are independent
        bucket1.setName("MODIFIED_BUCKET");
        assertEquals("MODIFIED_BUCKET", bucket1.getName());
        assertEquals("BUCKET_TWO", bucket2.getName()); // Should remain unchanged
        assertEquals("BUCKET_THREE", bucket3.getName()); // Should remain unchanged
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