package com.marvin.app.importer.sensors;

import com.marvin.app.service.ImportService;
import com.marvin.export.influxdb.dto.SensorDataDTO;
import com.marvin.influxdb.core.InfluxWriteConfig;
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
    public void importData(final InfluxWriteConfig config, final SensorDataDTO data) {
        if (data == null) {
            LOGGER.warn("Received null sensor data, skipping import");
            return;
        }

        try {
            sensorDataImport.importSensorData(config, data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to import sensor data", e);
        }
    }
}