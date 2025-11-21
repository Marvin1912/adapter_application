package com.marvin.app.sensor.importer;

import com.marvin.app.importer.FileTypeHandler;
import com.marvin.app.importer.ImportService;
import com.marvin.app.sensor.dto.SensorData;
import com.marvin.app.sensor.service.SensorDataImport;
import org.springframework.stereotype.Component;

/**
 * FileTypeHandler for processing sensor data files.
 *
 * <p>This component handles the processing of sensor data files by implementing
 * the FileTypeHandler interface. It can handle files with "sensor" in their name
 * and uses SensorDataImportService to process the data.</p>
 *
 * @author Marvin Application
 * @version 1.0
 * @since 1.0
 */
@Component
public class SensorDataFileTypeHandler implements FileTypeHandler<SensorData> {

    private final SensorDataImportService sensorDataImportService;

    /**
     * Constructs a SensorDataFileTypeHandler with the specified import service.
     *
     * @param sensorDataImportService the service for importing sensor data
     */
    public SensorDataFileTypeHandler(final SensorDataImportService sensorDataImportService) {
        this.sensorDataImportService = sensorDataImportService;
    }

    /**
     * Determines if this handler can process the specified file type.
     *
     * @param fileType the file type to check
     * @return true if the file type contains "sensor", false otherwise
     */
    @Override
    public boolean canHandle(final String fileType) {
        return fileType != null && fileType.toLowerCase().contains("sensor");
    }

    /**
     * Gets the DTO class that this handler processes.
     *
     * @return the SensorData class
     */
    @Override
    public Class<SensorData> getDtoClass() {
        return SensorData.class;
    }

    /**
     * Gets the import service for processing sensor data.
     *
     * @return the SensorDataImportService
     */
    @Override
    public ImportService<SensorData> getImportService() {
        return sensorDataImportService;
    }

    /**
     * Sends the sensor data DTO to the import service for processing.
     *
     * @param sensorData the sensor data to import
     */
    @Override
    public void send(final SensorData sensorData) {
        sensorDataImportService.importData(sensorData);
    }
}