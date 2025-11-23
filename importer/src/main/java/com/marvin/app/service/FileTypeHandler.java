package com.marvin.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface FileTypeHandler<T> {

    boolean canHandle(String fileType);

    Class<T> getDtoClass();

    default T readValue(String value, ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(value, getDtoClass());
        } catch (Exception e) {
            throw new RuntimeException("Could not parse " + value + " for type " + getDtoClass().getSimpleName(), e);
        }
    }

    void handle(T dto);
}