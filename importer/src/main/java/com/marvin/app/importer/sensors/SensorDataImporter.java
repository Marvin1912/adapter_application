package com.marvin.app.importer.sensors;

import com.marvin.app.service.FileTypeHandler;
import com.marvin.app.service.GenericFileReader;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SensorDataImporter {

    private final String in;
    private final GenericFileReader genericFileReader;
    private final List<FileTypeHandler<?>> fileTypeHandlers;

    public SensorDataImporter(
        @Value("${importer.in.sensordata}") String in,
        GenericFileReader genericFileReader,
        HumidityDataFileTypeHandler humidityDataFileTypeHandler,
        HumidityAggregatedDataFileTypeHandler humidityAggregatedDataFileTypeHandler,
        TemperatureDataFileTypeHandler temperatureDataFileTypeHandler,
        TemperatureAggregatedDataFileTypeHandler temperatureAggregatedDataFileTypeHandler
    ) {
        this.in = in;
        this.genericFileReader = genericFileReader;
        this.fileTypeHandlers = Arrays.asList(
            humidityDataFileTypeHandler,
            humidityAggregatedDataFileTypeHandler,
            temperatureDataFileTypeHandler,
            temperatureAggregatedDataFileTypeHandler
        );
    }

    public void importFiles() {
        genericFileReader.importFiles(in, fileTypeHandlers);
    }

}
