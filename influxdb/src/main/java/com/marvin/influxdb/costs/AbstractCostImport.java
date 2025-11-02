package com.marvin.influxdb.costs;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCostImport<DTO, MEAS> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCostImport.class);

    private final InfluxDBClient influxDBClient;

    public AbstractCostImport(InfluxDBClient influxDBClient) {
        this.influxDBClient = influxDBClient;
    }

    protected abstract MEAS map(DTO dto);

    protected Instant getAsInstant(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.of("UTC")).toInstant();
    }

    public void importCost(DTO dto) {
        MEAS measurement = map(dto);

        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        writeApi.writeMeasurement("costs", "wildfly_domain", WritePrecision.NS, measurement);

        LOGGER.info("Imported InfluxDB measurement {}", measurement);
    }
}
