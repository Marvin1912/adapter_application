package com.marvin.app.importer.sensors;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import java.time.Instant;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class SensorData {

    @Column(name = "measurement", tag = true)
    private String measurement;

    @Column(name = "entity_id", tag = true)
    private String entityId;

    @Column(name = "friendly_name", tag = true)
    private String friendlyName;

    @Column(timestamp = true)
    private Instant timestamp;

    @Column(name = "value")
    private Double field;

}