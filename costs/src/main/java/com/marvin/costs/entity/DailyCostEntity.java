package com.marvin.costs.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** JPA entity representing a single daily cost entry in the finance schema. */
@Entity
@Table(name = "daily_cost", schema = "finance")
public class DailyCostEntity extends BasicEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;

    @Basic
    @Column(name = "cost_date")
    private LocalDate costDate;

    @Basic
    @Column(name = "value")
    private BigDecimal value;

    @Basic
    @Column(name = "description")
    private String description;

    /** Default constructor required by JPA. */
    public DailyCostEntity() {
        // NOOP
    }

    /**
     * Constructs a new {@code DailyCostEntity} with the given fields.
     *
     * @param costDate    the date of the cost
     * @param value       the monetary value
     * @param description a short description
     */
    public DailyCostEntity(LocalDate costDate, BigDecimal value, String description) {
        this.costDate = costDate;
        this.value = value;
        this.description = description;
    }

    /**
     * Returns the cost date.
     *
     * @return the cost date
     */
    public LocalDate getCostDate() {
        return costDate;
    }

    /**
     * Sets the cost date.
     *
     * @param costDate the cost date to set
     */
    public void setCostDate(LocalDate costDate) {
        this.costDate = costDate;
    }

    /**
     * Returns the monetary value.
     *
     * @return the value
     */
    public BigDecimal getValue() {
        return value;
    }

    /**
     * Sets the monetary value.
     *
     * @param value the value to set
     */
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    /**
     * Returns the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final DailyCostEntity that = (DailyCostEntity) o;
        return id == that.id && Objects.equals(costDate, that.costDate)
                && Objects.equals(value, that.value)
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, costDate, value, description);
    }

    @Override
    public String toString() {
        return "DailyCostEntity{"
                + "id=" + id
                + ", costDate=" + costDate
                + ", value=" + value
                + ", description='" + description + '\''
                + '}';
    }
}
