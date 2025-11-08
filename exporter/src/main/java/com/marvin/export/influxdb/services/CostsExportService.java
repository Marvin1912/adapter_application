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
            final Object converted = DataTypeHandler.convertRecord(record, BUCKET_NAME);
            if (converted != null && converted instanceof CostsDTO dto) {
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
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the constructed Flux query string for energy costs
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
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the constructed Flux query string for infrastructure costs
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
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the constructed Flux query string for license costs
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
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the constructed Flux query string for maintenance costs
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
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the constructed Flux query string for subscription costs
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
     * @param currency the currency to filter costs by
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the constructed Flux query string for costs by currency
     */
    public String buildCostsByCurrencyQuery(String currency, Instant startTime, Instant endTime) {
        if (!MeasurementMappings.CostsMappings.SUPPORTED_CURRENCIES.contains(currency)) {
            final String errorMessage = "Unsupported currency: " + currency +
                ". Supported currencies: " + MeasurementMappings.CostsMappings.SUPPORTED_CURRENCIES;
            throw new IllegalArgumentException(errorMessage);
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
     * @param provider the provider to filter costs by
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the constructed Flux query string for costs by provider
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
     * @param billingPeriod the billing period to filter costs by
     * @param startTime the start time for the query
     * @param endTime the end time for the query
     * @return the constructed Flux query string for costs by billing period
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
     * @param date the specific date to query costs for
     * @return the constructed Flux query string for costs by date
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
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return the constructed Flux query string for costs by date range
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
     * @param startTime the optional start time for the export
     * @param endTime the optional end time for the export
     * @return a list of CostsDTO objects containing energy cost data
     */
    public java.util.List<CostsDTO> exportEnergyCosts(Instant startTime, Instant endTime) {
        final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
        final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

        final String query = buildEnergyCostsQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports infrastructure costs specifically.
     * @param startTime the optional start time for the export
     * @param endTime the optional end time for the export
     * @return a list of CostsDTO objects containing infrastructure cost data
     */
    public java.util.List<CostsDTO> exportInfrastructureCosts(Instant startTime, Instant endTime) {
        final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
        final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

        final String query = buildInfrastructureCostsQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports license costs specifically.
     * @param startTime the optional start time for the export
     * @param endTime the optional end time for the export
     * @return a list of CostsDTO objects containing license cost data
     */
    public java.util.List<CostsDTO> exportLicenseCosts(Instant startTime, Instant endTime) {
        final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
        final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

        final String query = buildLicenseCostsQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports maintenance costs specifically.
     * @param startTime the optional start time for the export
     * @param endTime the optional end time for the export
     * @return a list of CostsDTO objects containing maintenance cost data
     */
    public java.util.List<CostsDTO> exportMaintenanceCosts(Instant startTime, Instant endTime) {
        final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
        final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

        final String query = buildMaintenanceCostsQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports subscription costs specifically.
     * @param startTime the optional start time for the export
     * @param endTime the optional end time for the export
     * @return a list of CostsDTO objects containing subscription cost data
     */
    public java.util.List<CostsDTO> exportSubscriptionCosts(Instant startTime, Instant endTime) {
        final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
        final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

        final String query = buildSubscriptionCostsQuery(actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports costs by currency.
     * @param currency the currency to filter costs by
     * @param startTime the optional start time for the export
     * @param endTime the optional end time for the export
     * @return a list of CostsDTO objects containing cost data for the specified currency
     */
    public java.util.List<CostsDTO> exportByCurrency(String currency, Instant startTime,
            Instant endTime) {
        final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
        final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

        final String query = buildCostsByCurrencyQuery(currency, actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports costs by provider.
     * @param provider the provider to filter costs by
     * @param startTime the optional start time for the export
     * @param endTime the optional end time for the export
     * @return a list of CostsDTO objects containing cost data for the specified provider
     */
    public java.util.List<CostsDTO> exportByProvider(String provider, Instant startTime,
            Instant endTime) {
        final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
        final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

        final String query = buildCostsByProviderQuery(provider, actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports costs by billing period.
     * @param billingPeriod the billing period to filter costs by
     * @param startTime the optional start time for the export
     * @param endTime the optional end time for the export
     * @return a list of CostsDTO objects containing cost data for the specified billing period
     */
    public java.util.List<CostsDTO> exportByBillingPeriod(String billingPeriod, Instant startTime,
            Instant endTime) {
        final Instant actualStartTime = startTime != null ? startTime : getDefaultStartTime();
        final Instant actualEndTime = endTime != null ? endTime : getDefaultEndTime();

        final String query = buildCostsByBillingPeriodQuery(billingPeriod, actualStartTime, actualEndTime);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports costs for a specific date.
     * @param date the specific date to export costs for
     * @return a list of CostsDTO objects containing cost data for the specified date
     */
    public java.util.List<CostsDTO> exportByDate(LocalDate date) {
        final String query = buildCostsByDateQuery(date);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports costs within a date range.
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return a list of CostsDTO objects containing cost data for the specified date range
     */
    public java.util.List<CostsDTO> exportByDateRange(LocalDate startDate, LocalDate endDate) {
        final String query = buildCostsByDateRangeQuery(startDate, endDate);
        return executeQueryAndConvert(query);
    }

    /**
     * Exports costs for the current month.
     * @return a list of CostsDTO objects containing cost data for the current month
     */
    public java.util.List<CostsDTO> exportCurrentMonthCosts() {
        final LocalDate today = LocalDate.now();
        final LocalDate startOfMonth = today.withDayOfMonth(1);

        return exportByDateRange(startOfMonth, today);
    }

    /**
     * Exports costs for the previous month.
     * @return a list of CostsDTO objects containing cost data for the previous month
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
     * @param query the Flux query to execute
     * @return a list of CostsDTO objects containing the converted query results
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