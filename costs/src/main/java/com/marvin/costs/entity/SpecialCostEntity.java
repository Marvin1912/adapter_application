package com.marvin.costs.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;

/** JPA entity representing a special cost entry in the finance schema. */
@Entity
@Table(name = "special_cost", schema = "finance")
public class SpecialCostEntity extends BasicEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;

    @Basic
    @Column(name = "cost_date", nullable = false)
    private LocalDate costDate;

    /**
     * Returns the ID.
     *
     * @return the ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID.
     *
     * @param id the ID to set
     */
    public void setId(int id) {
        this.id = id;
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
        final SpecialCostEntity that = (SpecialCostEntity) o;
        return id == that.id && Objects.equals(costDate, that.costDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, costDate);
    }

    @Override
    public String toString() {
        return "SpecialCostEntity{"
                + "id=" + id
                + ", costDate=" + costDate
                + '}';
    }
}
