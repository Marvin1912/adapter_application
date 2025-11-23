package com.marvin.influxdb.core;

import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record GenericPojoImporter<T>(InfluxDBClient influxDBClient, InfluxWriteConfig config) {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericPojoImporter.class);

    public void importPojo(final T pojo) {
        validatePojo(pojo);

        final WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        writeApi.writeMeasurement(config.bucket(), config.organization(), config.writePrecision(), pojo);

        LOGGER.info("Successfully imported POJO of type {} to bucket {}",
            pojo.getClass().getSimpleName(), config.bucket());
    }

    private void validatePojo(final T pojo) {
        if (pojo == null) {
            throw new IllegalArgumentException("POJO cannot be null");
        }

        final Measurement measurement = pojo.getClass().getAnnotation(Measurement.class);
        if (measurement == null) {
            throw new IllegalArgumentException(
                String.format("POJO class %s must be annotated with @Measurement",
                    pojo.getClass().getSimpleName()));
        }

        if (measurement.name() == null || measurement.name().trim().isEmpty()) {
            throw new IllegalArgumentException(
                String.format("@Measurement annotation on class %s must specify a name",
                    pojo.getClass().getSimpleName()));
        }
    }

}
