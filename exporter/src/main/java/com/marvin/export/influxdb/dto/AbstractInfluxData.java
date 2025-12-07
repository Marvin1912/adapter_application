package com.marvin.export.influxdb.dto;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractInfluxData {

    protected String measurement;
    protected Long timestamp;
    protected Object field;
    protected Map<String, String> tags;

}
