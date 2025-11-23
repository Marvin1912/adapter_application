package com.marvin.app.importer.sensors;

import com.marvin.app.service.FileTypeHandler;
import com.marvin.export.influxdb.dto.SensorDataDTO;
import org.springframework.stereotype.Component;

@Component
public class HumidityAggregatedDataFileTypeHandler implements FileTypeHandler<SensorDataDTO> {

    private final SensorDataImportService sensorDataImportService;

    public HumidityAggregatedDataFileTypeHandler(final SensorDataImportService sensorDataImportService) {
        this.sensorDataImportService = sensorDataImportService;
    }

    @Override
    public boolean canHandle(final String fileType) {
        return "humidity_30m".equals(fileType);
    }

    @Override
    public Class<SensorDataDTO> getDtoClass() {
        return SensorDataDTO.class;
    }

    @Override
    public void handle(final SensorDataDTO sensorData) {
        sensorDataImportService.importData(sensorData);
    }
}