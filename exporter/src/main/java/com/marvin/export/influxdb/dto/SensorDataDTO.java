package com.marvin.export.influxdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SensorDataDTO extends AbstractInfluxData {

    public boolean isHumiditySensor() {
        return "%".equals(measurement);
    }

    public boolean isTemperatureSensor() {
        return "°C".equals(measurement);
    }

    public boolean isPowerSensor() {
        return "W".equals(measurement);
    }

}

