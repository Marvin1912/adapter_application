package com.marvin.app.importer.sensors;

import com.influxdb.client.InfluxDBClient;
import com.marvin.export.influxdb.dto.SensorDataDTO;
import com.marvin.influxdb.core.GenericPojoImporter;
import com.marvin.influxdb.core.InfluxWriteConfig;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SensorDataImport {

    private final InfluxDBClient influxDBClient;

    public SensorDataImport(final InfluxDBClient influxDBClient) {
        this.influxDBClient = influxDBClient;
    }

    public void importSensorData(final InfluxWriteConfig config, final SensorDataDTO sensorData) {

        final GenericPojoImporter<SensorData> genericPojoImporter = new GenericPojoImporter<>(influxDBClient, config);

        final SensorData data;
        if (sensorData.isHumiditySensor()) {
            data = new HumidityData(
                sensorData.getMeasurement(),
                sensorData.getTags().get("entity_id"),
                sensorData.getTags().get("friendly_name"),
                Instant.ofEpochMilli(sensorData.getTimestamp()),
                (double) sensorData.getField()
            );
        } else if (sensorData.isTemperatureSensor()) {
            data = new TemperatureData(
                sensorData.getMeasurement(),
                sensorData.getTags().get("entity_id"),
                sensorData.getTags().get("friendly_name"),
                Instant.ofEpochMilli(sensorData.getTimestamp()),
                (double) sensorData.getField()
            );
        } else if (sensorData.isPowerSensor()) {
            data = new PowerData(
                sensorData.getMeasurement(),
                sensorData.getTags().get("entity_id"),
                sensorData.getTags().get("friendly_name"),
                Instant.ofEpochMilli(sensorData.getTimestamp()),
                (double) sensorData.getField()
            );
        } else {
            throw new IllegalStateException("Sensor Data not supported");
        }

        genericPojoImporter.importPojo(data);
    }

}