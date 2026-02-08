package com.marvin.entities.costs;

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

    public DailyCostEntity() {
        // NOOP
    }

    public DailyCostEntity(LocalDate costDate, BigDecimal value, String description) {
        this.costDate = costDate;
        this.value = value;
        this.description = description;
    }

    public LocalDate getCostDate() {
        return costDate;
    }

    public void setCostDate(LocalDate costDate) {
        this.costDate = costDate;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

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
        DailyCostEntity that = (DailyCostEntity) o;
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
