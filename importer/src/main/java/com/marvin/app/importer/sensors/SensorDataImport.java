package com.marvin.app.importer.sensors;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.marvin.export.influxdb.dto.SensorDataDTO;
import com.marvin.influxdb.core.GenericPojoImporter;
import com.marvin.influxdb.core.InfluxWriteConfig;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SensorDataImport {

    private final GenericPojoImporter<SensorData> genericPojoImporter;

    public SensorDataImport(
        @Value("${influxdb.org}") String org,
        final InfluxDBClient influxDBClient
    ) {
        InfluxWriteConfig config = InfluxWriteConfig.create("sensor_data", org, WritePrecision.NS);
        this.genericPojoImporter = new GenericPojoImporter<>(influxDBClient, config);
    }

    public void importSensorData(final SensorDataDTO sensorData) {

        final SensorData data = new SensorData(
            sensorData.getMeasurement(),
            sensorData.getTags().get("entity_id"),
            sensorData.getTags().get("friendly_name"),
            Instant.ofEpochMilli(sensorData.getTimestamp()),
            (double) sensorData.getField()
        );

        genericPojoImporter.importPojo(data);
    }

}