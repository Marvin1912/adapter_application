package com.marvin.export.influxdb.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

/**
 * Data Transfer Object for cost data from the costs bucket.
 * Currently prepared for future cost-related metrics and financial data.
 * This bucket is currently empty but structured for when cost data becomes available.
 *
 * @param measurement the InfluxDB measurement name
 * @param costType the type of cost (e.g., "energy", "maintenance")
 * @param category the cost category for classification
 * @param description detailed description of the cost entry
 * @param costDate the date when the cost was incurred
 * @param timestamp the exact timestamp of the cost record
 * @param fields map of field names to their values
 * @param tags map of tag names to their values
 */
public record CostsDTO(
    String measurement,
    String costType,
    String category,
    String description,
    LocalDate costDate,
    Instant timestamp,
    Map<String, Object> fields,
    Map<String, String> tags
) {
    // Cost value getters
    public BigDecimal getValue() {
        final Object value = fields.get("value");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getAmount() {
        final Object value = fields.get("amount");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getCost() {
        final Object value = fields.get("cost");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    // Cost breakdown getters
    public BigDecimal getBaseCost() {
        final Object value = fields.get("base_cost");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getVariableCost() {
        final Object value = fields.get("variable_cost");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getFixedCost() {
        final Object value = fields.get("fixed_cost");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getTax() {
        final Object value = fields.get("tax");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    // Cost rate getters
    public BigDecimal getRate() {
        final Object value = fields.get("rate");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getPricePerUnit() {
        final Object value = fields.get("price_per_unit");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    // Usage/consumption getters
    public BigDecimal getUsage() {
        final Object value = fields.get("usage");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public BigDecimal getConsumption() {
        final Object value = fields.get("consumption");
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    public String getUnit() {
        final Object value = fields.get("unit");
        return value != null ? value.toString() : null;
    }

    // Currency and billing getters
    public String getCurrency() {
        return tags != null ? tags.get("currency") : null;
    }

    public String getBillingPeriod() {
        return tags != null ? tags.get("billing_period") : null;
    }

    public String getProvider() {
        return tags != null ? tags.get("provider") : null;
    }

    public String getService() {
        return tags != null ? tags.get("service") : null;
    }

    public String getAccount() {
        return tags != null ? tags.get("account") : null;
    }

    // Helper methods for cost categorization
    public boolean isEnergyCost() {
        return "energy".equals(costType) ||
               "electricity".equals(category) ||
               "power".equals(category);
    }

    public boolean isInfrastructureCost() {
        return "infrastructure".equals(costType) ||
               "hosting".equals(category) ||
               "server".equals(category);
    }

    public boolean isLicenseCost() {
        return "license".equals(costType) ||
               "software".equals(category);
    }

    public boolean isMaintenanceCost() {
        return "maintenance".equals(costType) ||
               "support".equals(category);
    }

    public boolean isOperationalCost() {
        return "operational".equals(costType) ||
               "operating".equals(category);
    }

    /**
     * Returns the primary cost value, trying different field names in order of preference.
     */
    public BigDecimal getPrimaryCostValue() {
        if (getValue() != null) return getValue();
        if (getAmount() != null) return getAmount();
        if (getCost() != null) return getCost();
        return null;
    }

    /**
     * Returns a formatted description of the cost entry.
     */
    public String getFormattedDescription() {
        final StringBuilder sb = new StringBuilder();

        if (costType != null) {
            sb.append(costType);
        }

        if (category != null && !category.equals(costType)) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(category);
        }

        if (description != null) {
            if (sb.length() > 0) sb.append(": ");
            sb.append(description);
        }

        return sb.length() > 0 ? sb.toString() : "Unknown cost";
    }

    /**
     * Returns true if this cost entry has a valid monetary value.
     */
    public boolean hasValidMonetaryValue() {
        return getPrimaryCostValue() != null && getPrimaryCostValue().doubleValue() > 0;
    }
}