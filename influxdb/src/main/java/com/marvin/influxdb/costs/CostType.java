package com.marvin.influxdb.costs;

/**
 * Enumeration representing different types of costs that can be imported into InfluxDB.
 *
 * <p>This enum defines the supported cost categories with their corresponding string values
 * used for tagging measurements in the InfluxDB database.</p>
 *
 * @author Marvin Application
 * @version 1.0
 * @since 1.0
 */
public enum CostType {

    /** Daily cost type for operational expenses. */
    DAILY("daily"),

    /** Monthly cost type for recurring monthly expenses. */
    MONTHLY("monthly"),

    /** Salary cost type for employee compensation. */
    SALARY("salary");

    private final String value;

    /**
     * Constructs a CostType with the specified string value.
     *
     * @param value the string representation of this cost type
     */
    CostType(final String value) {
        this.value = value;
    }

    /**
     * Returns the string value associated with this cost type.
     *
     * @return the string representation of this cost type
     */
    public String getValue() {
        return value;
    }
}
