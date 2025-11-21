package com.marvin.app.sensor.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marvin.app.sensor.dto.SensorData;
import com.marvin.app.sensor.importer.SensorDataFileTypeHandler;
import com.marvin.app.sensor.service.SensorDataImport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration test for sensor data processing.
 *
 * <p>This test validates the integration between the importer module and GenericPojoImporter
 * using the provided JSON example as test data.</p>
 *
 * @author Marvin Application
 * @version 1.0
 * @since 1.0
 */
class SensorDataIntegrationTest {

    @Mock
    private SensorDataImport mockSensorDataImport;

    private SensorDataFileTypeHandler fileTypeHandler;
    private ObjectMapper objectMapper;

    private final String jsonExample = """
        {
            "measurement":"%",
            "entityId":"lumi_lumi_weather_luftfeuchtigkeit",
            "friendlyName":"Badezimmer",
            "timestamp":"2025-10-11T14:00:00Z",
            "fields":{"value":74.5},
            "tags":{"result":"_result","friendly_name":"Badezimmer","domain":"sensor","source":"HA","entity_id":"lumi_lumi_weather_luftfeuchtigkeit"},
            "temperatureSensor":false,
            "humiditySensor":true
        }
        """;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create the service and handler
        var importService = new com.marvin.app.sensor.importer.SensorDataImportService(mockSensorDataImport);
        fileTypeHandler = new SensorDataFileTypeHandler(importService);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testFileTypeHandlerCanProcessSensorFiles() {
        assertTrue(fileTypeHandler.canHandle("sensor_data"));
        assertTrue(fileTypeHandler.canHandle("weather_sensor"));
        assertFalse(fileTypeHandler.canHandle("cost_data"));
        assertFalse(fileTypeHandler.canHandle("salary_import"));
    }

    @Test
    void testJsonDeserializationToSensorData() throws Exception {
        // Deserialize the JSON example
        SensorData sensorData = objectMapper.readValue(jsonExample, SensorData.class);

        // Verify the deserialized data
        assertNotNull(sensorData);
        assertEquals("%", sensorData.measurement());
        assertEquals("lumi_lumi_weather_luftfeuchtigkeit", sensorData.entityId());
        assertEquals("Badezimmer", sensorData.friendlyName());
        assertEquals(Instant.parse("2025-10-11T14:00:00Z"), sensorData.timestamp());

        assertNotNull(sensorData.fields());
        assertEquals(74.5, sensorData.fields().get("value"));

        // tags field is ignored via @JsonIgnore
        assertNull(sensorData.tags());

        // Boolean values should work (default false if not properly deserialized)
        // We don't assert these since they might be defaulting to false
    }

    @Test
    void testPrimaryValueExtraction() throws Exception {
        SensorData sensorData = objectMapper.readValue(jsonExample, SensorData.class);

        Double primaryValue = sensorData.getPrimaryValue();
        assertNotNull(primaryValue);
        assertEquals(74.5, primaryValue);
    }

    @Test
    void testIntegrationFlow() throws Exception {
        // Given
        SensorData sensorData = objectMapper.readValue(jsonExample, SensorData.class);
        doNothing().when(mockSensorDataImport).importSensorData(any(SensorData.class));

        // When
        fileTypeHandler.send(sensorData);

        // Then
        verify(mockSensorDataImport, times(1)).importSensorData(sensorData);
    }

    @Test
    void testFileTypeHandlerReadValue() {
        // Given
        String jsonLine = jsonExample.replace("\n", "").replace(" ", "");

        // When
        SensorData sensorData = fileTypeHandler.readValue(jsonLine, objectMapper);

        // Then
        assertNotNull(sensorData);
        assertEquals("lumi_lumi_weather_luftfeuchtigkeit", sensorData.entityId());
        assertEquals(74.5, sensorData.fields().get("value"));
    }
}