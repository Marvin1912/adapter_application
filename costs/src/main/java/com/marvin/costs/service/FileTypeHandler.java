package com.marvin.costs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marvin.influxdb.core.InfluxWriteConfig;

/**
 * Handler for a specific file type during cost data import.
 *
 * @param <T> the DTO type this handler processes
 */
public interface FileTypeHandler<T> {

    /**
     * Returns whether this handler can process the given file type.
     *
     * @param fileType the file type identifier
     * @return {@code true} if this handler can process the given file type
     */
    boolean canHandle(String fileType);

    /**
     * Returns the DTO class this handler processes.
     *
     * @return the DTO class
     */
    Class<T> getDtoClass();

    /**
     * Returns the InfluxDB bucket name for this handler.
     *
     * @return the bucket name
     */
    String getBucket();

    /**
     * Deserialises a JSON string into the DTO type of this handler.
     *
     * @param value        the JSON string to deserialise
     * @param objectMapper the ObjectMapper to use
     * @return the deserialised DTO
     */
    default T readValue(String value, ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(value, getDtoClass());
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not parse " + value + " for type " + getDtoClass().getSimpleName(), e);
        }
    }

    /**
     * Handles the given DTO.
     *
     * @param config the InfluxDB write configuration
     * @param dto    the DTO to handle
     */
    void handle(InfluxWriteConfig config, T dto);
}
