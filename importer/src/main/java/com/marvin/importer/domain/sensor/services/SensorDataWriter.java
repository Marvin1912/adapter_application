package com.marvin.importer.domain.sensor.services;

import com.influxdb.client.InfluxDBClient;
import com.marvin.influxdb.core.GenericPojoImporter;
import com.marvin.influxdb.core.InfluxWriteConfig;
import com.marvin.importer.data.sensor.SensorData;
import org.springframework.stereotype.Component;

@Component
public class SensorDataWriter {

    private final GenericPojoImporter<SensorData> genericPojoImporter;

    public SensorDataWriter(final InfluxDBClient influxDBClient) {
        InfluxWriteConfig config = InfluxWriteConfig.create("sensors", "wildfly_domain",
                                                            com.influxdb.client.domain.WritePrecision.S);
        this.genericPojoImporter = new GenericPojoImporter<>(influxDBClient, config);
    }

    public void writeSensorData(final SensorData sensorData) {
        genericPojoImporter.importPojo(sensorData);
    }

    public void writeSensorDataBatch(final java.util.List<SensorData> sensorDataList) {
        genericPojoImporter.importBatch(sensorDataList);
    }

    protected GenericPojoImporter<SensorData> getGenericPojoImporter() {
        return genericPojoImporter;
    }
}