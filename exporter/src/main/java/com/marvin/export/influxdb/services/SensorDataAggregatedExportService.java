package com.marvin.export.influxdb.services;

import com.influxdb.query.FluxRecord;
import com.marvin.export.influxdb.AbstractInfluxExport;
import com.marvin.export.influxdb.InfluxQueryBuilder;
import com.marvin.export.influxdb.dto.SensorDataAggregatedDTO;
import com.marvin.export.influxdb.handlers.DataTypeHandler;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Export service for aggregated sensor data from the sensor_data_30m bucket. Handles downsampled
 * humidity sensor data averaged to 30-minute intervals for long-term trend analysis.
 */
@Service
public class SensorDataAggregatedExportService extends
    AbstractInfluxExport<SensorDataAggregatedDTO> {

  private static final String BUCKET_NAME = "sensor_data_30m";

  @Override
  protected String getBucketName() {
    return BUCKET_NAME;
  }

  @Override
  protected String buildQuery(Instant startTime, Instant endTime) {
    return InfluxQueryBuilder.from(BUCKET_NAME)
        .timeRange(startTime, endTime)
        .measurements(
            // Aggregated sensor measurements we want to export
            "sensor_aggregated", "sensor_mean", "sensor_stats"
        )
        .keepOriginalColumns(true)
        .sort("desc") // Most recent first
        .build();
  }

  @Override
  protected Optional<SensorDataAggregatedDTO> convertRecord(FluxRecord record) {
    try {
      // Use the DataTypeHandler to convert the record
      final Object converted = DataTypeHandler.convertRecord(record, BUCKET_NAME);
      if (converted instanceof SensorDataAggregatedDTO dto) {
        // Validate the DTO using DataTypeHandler
        if (DataTypeHandler.validateDTO(dto, BUCKET_NAME)) {
          return Optional.of(dto);
        } else {
          LOGGER.debug("SensorDataAggregatedDTO validation failed for record: {}", record);
        }
      }
    } catch (Exception e) {
      LOGGER.error("Failed to convert aggregated sensor data record: {}", record, e);
    }
    return Optional.empty();
  }

  /**
   * Builds a query for aggregated humidity sensor data only.
   *
   * @param startTime the start time for the query
   * @param endTime   the end time for the query
   * @return the constructed Flux query string for humidity data
   */
  public String buildAggregatedHumidityQuery(Instant startTime, Instant endTime) {
    return InfluxQueryBuilder.from(BUCKET_NAME)
        .timeRange(startTime, endTime)
        .measurement("%")
        .field("value")
        .map("""
            fn: (r) => ({
                  r with friendly_name:
                    if r.entity_id == "lumi_lumi_weather_luftfeuchtigkeit" then "Badezimmer"
                    else if r.entity_id == "lumi_lumi_weather_luftfeuchtigkeit_2" then "Flur"
                    else if r.entity_id == "lumi_lumi_weather_luftfeuchtigkeit_3" then "Küche"
                    else if r.entity_id == "lumi_lumi_weather_luftfeuchtigkeit_4" then "Schlafzimmer"
                    else if r.entity_id == "lumi_lumi_weather_luftfeuchtigkeit_5" then "Wohnzimmer"
                    else "Nicht bekannt"
                })""")
        .keepOriginalColumns(false)
        .sort("desc")
        .build();
  }

  /**
   * Builds a query for aggregated temperature sensor data only.
   *
   * @param startTime the start time for the query
   * @param endTime   the end time for the query
   * @return the constructed Flux query string for temperature data
   */
  public String buildAggregatedTemperatureQuery(Instant startTime, Instant endTime) {
    return InfluxQueryBuilder.from(BUCKET_NAME)
        .timeRange(startTime, endTime)
        .measurement("°C")
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
   * Exports aggregated humidity sensor data specifically.
   *
   * @param startTime optional start time for the export, defaults to configured default
   * @param endTime   optional end time for the export, defaults to configured default
   * @return list of aggregated humidity sensor data DTOs
   */
  public List<SensorDataAggregatedDTO> exportAggregatedHumidity(
      Instant startTime, Instant endTime) {
    final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
    final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

    final String query = buildAggregatedHumidityQuery(actualStartTime, actualEndTime);
    return executeQueryAndConvert(query);
  }

  /**
   * Exports aggregated temperature sensor data specifically.
   *
   * @param startTime optional start time for the export, defaults to configured default
   * @param endTime   optional end time for the export, defaults to configured default
   * @return list of aggregated temperature sensor data DTOs
   */
  public List<SensorDataAggregatedDTO> exportAggregatedTemperature(
      Instant startTime, Instant endTime) {
    final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
    final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

    final String query = buildAggregatedTemperatureQuery(actualStartTime, actualEndTime);
    return executeQueryAndConvert(query);
  }

  /**
   * Main export method that returns temperature data for streaming. Wrapper method for
   * exportAggregatedTemperature to match interface.
   *
   * @param startTime the optional start time for export
   * @param endTime   the optional end time for export
   * @return stream of temperature sensor data objects
   */
  public java.util.stream.Stream<SensorDataAggregatedDTO> exportTemperatureData(Instant startTime,
      Instant endTime) {
    return exportAggregatedTemperature(startTime, endTime).stream();
  }

  /**
   * Main export method that returns humidity data for streaming. Wrapper method for
   * exportAggregatedHumidity to match interface.
   *
   * @param startTime the optional start time for export
   * @param endTime   the optional end time for export
   * @return stream of humidity sensor data objects
   */
  public java.util.stream.Stream<SensorDataAggregatedDTO> exportHumidityData(Instant startTime,
      Instant endTime) {
    return exportAggregatedHumidity(startTime, endTime).stream();
  }

  /**
   * Helper method to execute a query and convert results.
   *
   * @param query the Flux query to execute
   * @return list of converted aggregated sensor data DTOs
   */
  private List<SensorDataAggregatedDTO> executeQueryAndConvert(String query) {
    try {
      return executeQuery(query).stream()
          .flatMap(table -> table.getRecords().stream())
          .map(this::convertRecord)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .toList();
    } catch (Exception e) {
      LOGGER.error("Failed to execute aggregated sensor data query: {}", query, e);
      throw new InfluxExportException("Failed to execute aggregated sensor data query", e);
    }
  }
}