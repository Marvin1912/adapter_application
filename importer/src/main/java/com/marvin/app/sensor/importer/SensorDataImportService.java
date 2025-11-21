package com.marvin.app.sensor.importer;

import com.marvin.app.importer.ImportService;
import com.marvin.app.sensor.dto.SensorData;
import com.marvin.app.sensor.service.SensorDataImport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Import service for processing sensor data.
 *
 * <p>This service implements the ImportService interface to handle the import
 * of sensor data using the SensorDataImport component which internally uses
 * the GenericPojoImporter.</p>
 *
 * @author Marvin Application
 * @version 1.0
 * @since 1.0
 */
@Service
public class SensorDataImportService implements ImportService<SensorData> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorDataImportService.class);

    private final SensorDataImport sensorDataImport;

    /**
     * Constructs a SensorDataImportService with the specified sensor data import component.
     *
     * @param sensorDataImport the component for importing sensor data to InfluxDB
     */
    public SensorDataImportService(final SensorDataImport sensorDataImport) {
        this.sensorDataImport = sensorDataImport;
    }

    /**
     * Imports sensor data into InfluxDB.
     *
     * <p>This method processes the incoming sensor data and forwards it to the
     * SensorDataImport component which uses GenericPojoImporter to write it to InfluxDB.</p>
     *
     * @param data the sensor data to import
     */
    @Override
    public void importData(final SensorData data) {
        if (data == null) {
            LOGGER.warn("Received null sensor data, skipping import");
            return;
        }

        LOGGER.info("Importing sensor data for entity: {} ({})",
                   data.entityId(), data.friendlyName());

        try {
            sensorDataImport.importSensorData(data);
            LOGGER.info("Successfully imported sensor data for entity: {}", data.entityId());
        } catch (Exception e) {
            LOGGER.error("Failed to import sensor data for entity: {}", data.entityId(), e);
            throw new RuntimeException("Failed to import sensor data", e);
        }
    }
}