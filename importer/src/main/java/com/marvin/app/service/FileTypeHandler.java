package com.marvin.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marvin.influxdb.core.InfluxWriteConfig;

public interface FileTypeHandler<T> {

    boolean canHandle(String fileType);

    Class<T> getDtoClass();

    String getBucket();

    default T readValue(String value, ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(value, getDtoClass());
        } catch (Exception e) {
            throw new RuntimeException("Could not parse " + value + " for type " + getDtoClass().getSimpleName(), e);
        }
    }

    void handle(InfluxWriteConfig config, T dto);
}