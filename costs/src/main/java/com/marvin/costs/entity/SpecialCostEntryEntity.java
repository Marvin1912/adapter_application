package com.marvin.costs.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;

/** JPA entity representing a line-item entry within a special cost in the finance schema. */
@Entity
@Table(name = "special_cost_entry", schema = "finance")
public class SpecialCostEntryEntity extends BasicEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;

    @Basic
    @Column(name = "description", nullable = false, length = 2048)
    private String description;

    @Basic
    @Column(name = "additional_info", nullable = false, length = 2048)
    private String additionalInfo;

    @Basic
    @Column(name = "value", nullable = false, precision = 2)
    private BigDecimal value;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "special_cost_id")
    private SpecialCostEntity specialCost;

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

    /**
     * Returns the additional info.
     *
     * @return the additional info
     */
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * Sets the additional info.
     *
     * @param additionalInfo the additional info to set
     */
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
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
     * Returns the associated special cost.
     *
     * @return the special cost entity
     */
    public SpecialCostEntity getSpecialCost() {
        return specialCost;
    }

    /**
     * Sets the associated special cost.
     *
     * @param specialCost the special cost entity to associate
     */
    public void setSpecialCost(SpecialCostEntity specialCost) {
        this.specialCost = specialCost;
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
        final SpecialCostEntryEntity that = (SpecialCostEntryEntity) o;
        return id == that.id
                && Objects.equals(description, that.description)
                && Objects.equals(value, that.value)
                && Objects.equals(specialCost, that.specialCost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, description, value, specialCost);
    }

    @Override
    public String toString() {
        return "SpecialCostEntryEntity{"
                + "id=" + id
                + ", description='" + description + '\''
                + ", value=" + value
                + ", specialCost=" + specialCost
                + '}';
    }
}
