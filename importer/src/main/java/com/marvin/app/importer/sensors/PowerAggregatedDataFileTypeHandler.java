package com.marvin.app.importer.sensors;

import com.marvin.app.service.FileTypeHandler;
import com.marvin.export.influxdb.InfluxExporter;
import com.marvin.export.influxdb.dto.SensorDataDTO;
import com.marvin.influxdb.core.InfluxWriteConfig;
import org.springframework.stereotype.Component;

@Component
public class PowerAggregatedDataFileTypeHandler implements FileTypeHandler<SensorDataDTO> {

    private final SensorDataImportService sensorDataImportService;

    public PowerAggregatedDataFileTypeHandler(final SensorDataImportService sensorDataImportService) {
        this.sensorDataImportService = sensorDataImportService;
    }

    @Override
    public boolean canHandle(final String fileType) {
        return InfluxExporter.POWER_AGGREGATED_FILENAME_PREFIX.equals(fileType);
    }

    @Override
    public Class<SensorDataDTO> getDtoClass() {
        return SensorDataDTO.class;
    }

    @Override
    public String getBucket() {
        return "sensor_data_30m";
    }

    @Override
    public void handle(final InfluxWriteConfig config, final SensorDataDTO sensorData) {
        sensorDataImportService.importData(config, sensorData);
    }
}