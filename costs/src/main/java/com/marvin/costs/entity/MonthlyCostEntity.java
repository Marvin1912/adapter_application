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

/** JPA entity representing a monthly cost entry in the finance schema. */
@Entity
@Table(name = "monthly_cost", schema = "finance")
public class MonthlyCostEntity extends BasicEntity {

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

    /** Default constructor required by JPA. */
    public MonthlyCostEntity() {
        // NOOP
    }

    /**
     * Constructs a new {@code MonthlyCostEntity} with the given fields.
     *
     * @param costDate the date of the monthly cost
     * @param value    the monetary value
     */
    public MonthlyCostEntity(LocalDate costDate, BigDecimal value) {
        this.costDate = costDate;
        this.value = value;
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
        final MonthlyCostEntity that = (MonthlyCostEntity) o;
        return id == that.id && Objects.equals(costDate, that.costDate) && Objects.equals(value,
                that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, costDate, value);
    }

    @Override
    public String toString() {
        return "MonthlyCostEntity{"
                + "id=" + id
                + ", costDate=" + costDate
                + ", value=" + value
                + '}';
    }
}
