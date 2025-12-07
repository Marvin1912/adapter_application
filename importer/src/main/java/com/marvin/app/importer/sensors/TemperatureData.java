package com.marvin.app.importer.sensors;

import com.influxdb.annotations.Measurement;
import java.time.Instant;

@Measurement(name = "°C")
public class TemperatureData extends SensorData {

    public TemperatureData(String measurement, String entityId, String friendlyName, Instant timestamp, Double field) {
        super(measurement, entityId, friendlyName, timestamp, field);
    }
}