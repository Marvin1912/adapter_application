package com.marvin.app.importer.sensors;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import java.time.Instant;

@Measurement(name = "%")
public record SensorData(
    @Column(name = "measurement", tag = true)
    String measurement,

    @Column(name = "entity_id", tag = true)
    String entityId,

    @Column(name = "friendly_name", tag = true)
    String friendlyName,

    @Column(timestamp = true)
    Instant timestamp,

    @Column(name = "value")
    Double field
) {

}