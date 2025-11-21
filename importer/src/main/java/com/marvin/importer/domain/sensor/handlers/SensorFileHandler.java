package com.marvin.importer.domain.sensor.handlers;

import com.marvin.importer.core.FileTypeHandler;
import com.marvin.importer.core.ImportService;
import com.marvin.importer.data.sensor.SensorData;
import com.marvin.importer.domain.sensor.services.SensorImportService;
import org.springframework.stereotype.Component;

@Component
public class SensorFileHandler implements FileTypeHandler<SensorData> {

    private final SensorImportService sensorImportService;

    public SensorFileHandler(final SensorImportService sensorImportService) {
        this.sensorImportService = sensorImportService;
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
        return sensorImportService;
    }

    @Override
    public void send(final SensorData sensorData) {
        sensorImportService.importData(sensorData);
    }
}