package com.marvin.app.sensor.importer;

import com.marvin.app.importer.FileTypeHandler;
import com.marvin.app.importer.ImportService;
import com.marvin.app.sensor.dto.SensorData;
import com.marvin.app.sensor.service.SensorDataImport;
import org.springframework.stereotype.Component;

@Component
public class SensorDataFileTypeHandler implements FileTypeHandler<SensorData> {

    private final SensorDataImportService sensorDataImportService;

    public SensorDataFileTypeHandler(final SensorDataImportService sensorDataImportService) {
        this.sensorDataImportService = sensorDataImportService;
    }

    @Override
    public boolean canHandle(final String fileType) {
        return fileType != null && fileType.toLowerCase().contains("sensor");
    }

    @Override
    public Class<SensorData> getDtoClass() {
        return SensorData.class;
    }

    @Override
    public ImportService<SensorData> getImportService() {
        return sensorDataImportService;
    }

    @Override
    public void send(final SensorData sensorData) {
        sensorDataImportService.importData(sensorData);
    }
}