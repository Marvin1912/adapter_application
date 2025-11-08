package com.marvin.export.influxdb.services;

import com.influxdb.query.FluxRecord;
import com.marvin.export.influxdb.AbstractInfluxExport;
import com.marvin.export.influxdb.InfluxQueryBuilder;
import com.marvin.export.influxdb.dto.CostsDTO;
import com.marvin.export.influxdb.handlers.DataTypeHandler;
import com.marvin.export.influxdb.mappings.MeasurementMappings;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Export service for cost data from the costs bucket.
 * Currently prepared for future cost-related metrics and financial data.
 * This bucket is currently empty but structured for when cost data becomes available.
 */
@Service
public class CostsExportService extends AbstractInfluxExport<CostsDTO> {

    private static final String BUCKET_NAME = "costs";

    @Override
    protected String getBucketName() {
        return BUCKET_NAME;
    }

    @Override
    protected String buildQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements(
                    // Cost-related measurements we want to export
                    "costs", "expenses", "billing", "subscriptions", "licenses"
                )
                .keepOriginalColumns(true)
                .sort("desc") // Most recent first
                .build();
    }

    @Override
    protected Optional<CostsDTO> convertRecord(FluxRecord record) {
        try {
            // Use the DataTypeHandler to convert the record
            final Optional<?> converted = DataTypeHandler.convertRecord(record, BUCKET_NAME);
            if (converted.isPresent() && converted.get() instanceof CostsDTO dto) {
                // Validate the DTO using DataTypeHandler
                if (DataTypeHandler.validateDTO(dto, BUCKET_NAME)) {
                    return Optional.of(dto);
                } else {
                    LOGGER.debug("CostsDTO validation failed for record: {}", record);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to convert cost data record: {}", record, e);
        }
        return Optional.empty();
    }

    @Override
    protected String getDataTypeDescription() {
        return "Cost and financial metrics (prepared for future cost data)";
    }

    /**
     * Builds a query for energy costs specifically.
     */
    public String buildEnergyCostsQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurement("costs")
                .tag("cost_type", "energy")
                .tagRegex("category", "(electricity|power|energy)")
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for infrastructure costs specifically.
     */
    public String buildInfrastructureCostsQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("costs", "expenses")
                .tag("cost_type", "infrastructure")
                .tagRegex("category", "(hosting|server|cloud)")
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for license costs specifically.
     */
    public String buildLicenseCostsQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("costs", "licenses")
                .tag("cost_type", "license")
                .tagRegex("category", "(software|license)")
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for maintenance costs specifically.
     */
    public String buildMaintenanceCostsQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("costs", "expenses")
                .tagRegex("cost_type", "(maintenance|support)")
                .tagRegex("category", "(maintenance|support)")
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for subscription costs specifically.
     */
    public String buildSubscriptionCostsQuery(Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("costs", "subscriptions")
                .tag("cost_type", "subscription")
                .tagRegex("category", "(subscription|service)")
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for costs by currency.
     */
    public String buildCostsByCurrencyQuery(String currency, Instant startTime, Instant endTime) {
        if (!MeasurementMappings.CostsMappings.SUPPORTED_CURRENCIES.contains(currency)) {
            throw new IllegalArgumentException("Unsupported currency: " + currency +
                ". Supported currencies: " + MeasurementMappings.CostsMappings.SUPPORTED_CURRENCIES);
        }

        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("costs", "expenses", "billing")
                .tag("currency", currency)
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for costs by provider.
     */
    public String buildCostsByProviderQuery(String provider, Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("costs", "expenses", "billing")
                .tag("provider", provider)
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for costs by billing period.
     */
    public String buildCostsByBillingPeriodQuery(String billingPeriod, Instant startTime, Instant endTime) {
        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("costs", "billing")
                .tag("billing_period", billingPeriod)
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for costs for a specific date.
     */
    public String buildCostsByDateQuery(LocalDate date) {
        final Instant startTime = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        final Instant endTime = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("costs", "expenses", "billing")
                .sort("desc")
                .build();
    }

    /**
     * Builds a query for costs within a date range.
     */
    public String buildCostsByDateRangeQuery(LocalDate startDate, LocalDate endDate) {
        final Instant startTime = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        final Instant endTime = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        return InfluxQueryBuilder.from(BUCKET_NAME)
                .timeRange(startTime, endTime)
                .measurements("costs", "expenses", "billing")
                .sort("desc")
                .build();
    }

    /**
     * Exports energy costs specifically.
     */
    public java.util.List<CostsDTO> exportEnergyCosts(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildEnergyCostsQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports infrastructure costs specifically.
     */
    public java.util.List<CostsDTO> exportInfrastructureCosts(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildInfrastructureCostsQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports license costs specifically.
     */
    public java.util.List<CostsDTO> exportLicenseCosts(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildLicenseCostsQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports maintenance costs specifically.
     */
    public java.util.List<CostsDTO> exportMaintenanceCosts(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildMaintenanceCostsQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports subscription costs specifically.
     */
    public java.util.List<CostsDTO> exportSubscriptionCosts(Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildSubscriptionCostsQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports costs by currency.
     */
    public java.util.List<CostsDTO> exportByCurrency(String currency, Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildCostsByCurrencyQuery(currency, actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports costs by provider.
     */
    public java.util.List<CostsDTO> exportByProvider(String provider, Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildCostsByProviderQuery(provider, actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports costs by billing period.
     */
    public java.util.List<CostsDTO> exportByBillingPeriod(String billingPeriod, Optional<Instant> startTime, Optional<Instant> endTime) {
        final Instant actualStartTime = startTime.orElse(getDefaultStartTime());
        final Instant actualEndTime = endTime.orElse(getDefaultEndTime());

        final String query = buildCostsByBillingPeriodQuery(billingPeriod, actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports costs for a specific date.
     */
    public java.util.List<CostsDTO> exportByDate(LocalDate date) {
        final String query = buildCostsByDateQuery(date);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports costs within a date range.
     */
    public java.util.List<CostsDTO> exportByDateRange(LocalDate startDate, LocalDate endDate) {
        final String query = buildCostsByDateRangeQuery(startDate, endDate);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports costs for the current month.
     */
    public java.util.List<CostsDTO> exportCurrentMonthCosts() {
        final LocalDate today = LocalDate.now();
        final LocalDate startOfMonth = today.withDayOfMonth(1);

        return exportByDateRange(startOfMonth, today);
    }

    /**
     * Exports costs for the previous month.
     */
    public java.util.List<CostsDTO> exportPreviousMonthCosts() {
        final LocalDate today = LocalDate.now();
        final LocalDate startOfCurrentMonth = today.withDayOfMonth(1);
        final LocalDate startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);
        final LocalDate endOfPreviousMonth = startOfCurrentMonth.minusDays(1);

        return exportByDateRange(startOfPreviousMonth, endOfPreviousMonth);
    }

    /**
     * Helper method to execute a query and convert results.
     */
    private java.util.List<CostsDTO> executeQueryAndConvert(String query) {
        try {
            return executeQuery(query).stream()
                    .flatMap(table -> table.getRecords().stream())
                    .map(this::convertRecord)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        } catch (Exception e) {
            LOGGER.error("Failed to execute cost data query: {}", query, e);
            throw new InfluxExportException("Failed to execute cost data query", e);
        }
    }
}