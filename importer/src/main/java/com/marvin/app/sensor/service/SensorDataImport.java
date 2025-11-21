package com.marvin.app.sensor.service;

import com.influxdb.client.InfluxDBClient;
import com.marvin.influxdb.core.GenericPojoImporter;
import com.marvin.influxdb.core.InfluxWriteConfig;
import com.marvin.app.sensor.dto.SensorData;
import org.springframework.stereotype.Component;

@Component
public class SensorDataImport {

    private final GenericPojoImporter<SensorData> genericPojoImporter;

    public SensorDataImport(final InfluxDBClient influxDBClient) {
        InfluxWriteConfig config = InfluxWriteConfig.create("sensors", "wildfly_domain",
                                                            com.influxdb.client.domain.WritePrecision.S);
        this.genericPojoImporter = new GenericPojoImporter<>(influxDBClient, config);
    }

    public void importSensorData(final SensorData sensorData) {
        genericPojoImporter.importPojo(sensorData);
    }

    public void importSensorDataBatch(final java.util.List<SensorData> sensorDataList) {
        genericPojoImporter.importBatch(sensorDataList);
    }

    protected GenericPojoImporter<SensorData> getGenericPojoImporter() {
        return genericPojoImporter;
    }
}