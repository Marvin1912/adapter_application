package com.marvin.app.sensor.service;

import com.influxdb.client.InfluxDBClient;
import com.marvin.influxdb.core.GenericPojoImporter;
import com.marvin.influxdb.core.InfluxWriteConfig;
import com.marvin.app.sensor.dto.SensorData;
import org.springframework.stereotype.Component;

/**
 * Service class for importing sensor data into InfluxDB.
 *
 * <p>This component provides functionality for importing sensor data using the GenericPojoImporter.
 * It handles the mapping and validation of sensor data for storage in InfluxDB.</p>
 *
 * @author Marvin Application
 * @version 1.0
 * @since 1.0
 */
@Component
public class SensorDataImport {

    private final GenericPojoImporter<SensorData> genericPojoImporter;

    /**
     * Constructs a SensorDataImport with the specified InfluxDB client.
     *
     * @param influxDBClient the InfluxDB client to use for writing sensor data
     */
    public SensorDataImport(final InfluxDBClient influxDBClient) {
        // Create a config for sensor data - you might want to make this configurable
        InfluxWriteConfig config = InfluxWriteConfig.create("sensors", "wildfly_domain",
                                                            com.influxdb.client.domain.WritePrecision.S);
        this.genericPojoImporter = new GenericPojoImporter<>(influxDBClient, config);
    }

    /**
     * Imports a single sensor data record into InfluxDB.
     *
     * @param sensorData the sensor data to import
     */
    public void importSensorData(final SensorData sensorData) {
        genericPojoImporter.importPojo(sensorData);
    }

    /**
     * Imports multiple sensor data records into InfluxDB in batch.
     *
     * @param sensorDataList the list of sensor data records to import
     */
    public void importSensorDataBatch(final java.util.List<SensorData> sensorDataList) {
        genericPojoImporter.importBatch(sensorDataList);
    }

    /**
     * Gets the underlying GenericPojoImporter for advanced usage.
     *
     * @return the GenericPojoImporter instance
     */
    protected GenericPojoImporter<SensorData> getGenericPojoImporter() {
        return genericPojoImporter;
    }
}