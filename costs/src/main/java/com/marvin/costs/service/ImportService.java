package com.marvin.costs.service;

import com.marvin.influxdb.core.InfluxWriteConfig;

/**
 * Generic contract for importing data of type {@code T}.
 *
 * @param <T> the type of data to import
 */
public interface ImportService<T> {

    /**
     * Imports the given data using the provided InfluxDB write configuration.
     *
     * @param config the InfluxDB write configuration, may be {@code null}
     * @param data   the data to import
     */
    void importData(InfluxWriteConfig config, T data);

    /**
     * Imports the given data without an InfluxDB write configuration.
     *
     * @param data the data to import
     */
    default void importData(T data) {
        importData(null, data);
    }
}
