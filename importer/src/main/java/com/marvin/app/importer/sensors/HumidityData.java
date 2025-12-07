package com.marvin.app.importer.sensors;

import com.influxdb.annotations.Measurement;
import java.time.Instant;

@Measurement(name = "%")
public class HumidityData extends SensorData {

    public HumidityData(String measurement, String entityId, String friendlyName, Instant timestamp, Double field) {
        super(measurement, entityId, friendlyName, timestamp, field);
    }
}