package com.marvin.importer.domain.sensor.services;

import com.marvin.importer.core.ImportService;
import com.marvin.importer.data.sensor.SensorData;
import com.marvin.importer.domain.sensor.services.SensorDataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SensorImportService implements ImportService<SensorData> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorImportService.class);

    private final SensorDataWriter sensorDataWriter;

    public SensorImportService(final SensorDataWriter sensorDataWriter) {
        this.sensorDataWriter = sensorDataWriter;
    }

    @Override
    public void importData(final SensorData data) {
        if (data == null) {
            LOGGER.warn("Received null sensor data, skipping import");
            return;
        }

        LOGGER.info("Importing sensor data for entity: {} ({})",
                   data.entityId(), data.friendlyName());

        try {
            sensorDataWriter.writeSensorData(data);
            LOGGER.info("Successfully imported sensor data for entity: {}", data.entityId());
        } catch (Exception e) {
            LOGGER.error("Failed to import sensor data for entity: {}", data.entityId(), e);
            throw new RuntimeException("Failed to import sensor data", e);
        }
    }
}