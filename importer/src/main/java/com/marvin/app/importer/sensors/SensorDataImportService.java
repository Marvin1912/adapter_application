package com.marvin.app.importer.sensors;

import com.marvin.app.service.ImportService;
import com.marvin.export.influxdb.dto.SensorDataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SensorDataImportService implements ImportService<SensorDataDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorDataImportService.class);

    private final SensorDataImport sensorDataImport;

    public SensorDataImportService(final SensorDataImport sensorDataImport) {
        this.sensorDataImport = sensorDataImport;
    }

    @Override
    public void importData(final SensorDataDTO data) {
        if (data == null) {
            LOGGER.warn("Received null sensor data, skipping import");
            return;
        }

        LOGGER.info("Importing sensor data for entity: {} ({})", data.getEntityId(), data.getFriendlyName());

        try {
            sensorDataImport.importSensorData(data);
            LOGGER.info("Successfully imported sensor data for entity: {}", data.getEntityId());
        } catch (Exception e) {
            LOGGER.error("Failed to import sensor data for entity: {}", data.getEntityId(), e);
            throw new RuntimeException("Failed to import sensor data", e);
        }
    }
}