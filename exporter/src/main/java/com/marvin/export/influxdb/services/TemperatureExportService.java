package com.marvin.export.influxdb.services;

import com.influxdb.query.FluxRecord;
import com.marvin.export.influxdb.AbstractInfluxExport;
import com.marvin.export.influxdb.InfluxQueryBuilder;
import com.marvin.export.influxdb.dto.SensorDataDTO;
import com.marvin.export.influxdb.handlers.DataTypeHandler;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

/**
 * Export service for temperature sensor data from the sensor_data bucket.
 * Handles temperature readings from various sensors including Home Assistant climate devices.
 */
@Service
public class TemperatureExportService extends AbstractInfluxExport<SensorDataDTO> {

    private static final String BUCKET_NAME = "sensor_data";

    @Override
    protected String getBucketName() {
        return BUCKET_NAME;
    }

    @Override
    protected String buildQuery(Instant startTime, Instant endTime) {
        return buildTemperatureSensorQuery(startTime, endTime);
    }

    @Override
    protected Optional<SensorDataDTO> convertRecord(FluxRecord record) {
        try {
            // Use the DataTypeHandler to convert the record
            final Object converted = DataTypeHandler.convertRecord(record, BUCKET_NAME);
            if (converted instanceof SensorDataDTO dto) {
                // Filter to include only temperature records
                if (dto.isTemperatureSensor() && DataTypeHandler.validateDTO(dto, BUCKET_NAME)) {
                    return Optional.of(dto);
                } else {
                    LOGGER.debug("Non-temperature sensor record filtered out: {}", record);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to convert temperature sensor data record: {}", record, e);
        }
        return Optional.empty();
    }

    /**
     * Builds a query for temperature sensor data only.
     *
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the Flux query string for temperature sensors
     */
    public String buildTemperatureSensorQuery(Instant startTime, Instant endTime) {
      return InfluxQueryBuilder.from(BUCKET_NAME)
          .timeRange(startTime, endTime)
          .measurement("%")
          .field("value")
          .map("""
                    fn: (r) => ({
                          r with friendly_name:
                            if r.entity_id == "lumi_lumi_weather_temperatur" then "Badezimmer"
                            else if r.entity_id == "lumi_lumi_weather_temperatur_2" then "Flur"
                            else if r.entity_id == "lumi_lumi_weather_temperatur_3" then "Küche"
                            else if r.entity_id == "lumi_lumi_weather_temperatur_4" then "Schlafzimmer"
                            else if r.entity_id == "lumi_lumi_weather_temperatur_5" then "Wohnzimmer"
                            else "Nicht bekannt"
                        })""")
          .keepOriginalColumns(false)
          .sort("desc")
          .build();
    }

    /**
     * Exports temperature sensor data specifically.
     *
     * @param startTime the optional start time for export
     * @param endTime the optional end time for export
     * @return list of temperature sensor data objects
     */
    public List<SensorDataDTO> exportTemperatureSensors(
            Instant startTime, Instant endTime) {
        final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
        final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

        final String query = buildTemperatureSensorQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Main export method that returns temperature data for streaming.
     *
     * @param startTime the optional start time for export
     * @param endTime the optional end time for export
     * @return stream of temperature sensor data objects
     */
    public Stream<SensorDataDTO> exportTemperatureData(Instant startTime, Instant endTime) {
        return exportTemperatureSensors(startTime, endTime).stream();
    }

    /**
     * Helper method to execute a query and convert results.
     *
     * @param query the Flux query to execute
     * @return list of converted temperature sensor data DTOs
     */
    private List<SensorDataDTO> executeQueryAndConvert(String query) {
        try {
            return executeQuery(query).stream()
                    .flatMap(table -> table.getRecords().stream())
                    .map(this::convertRecord)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        } catch (Exception e) {
            LOGGER.error("Failed to execute temperature sensor data query: {}", query, e);
            throw new RuntimeException("Failed to execute temperature sensor data query", e);
        }
    }
}