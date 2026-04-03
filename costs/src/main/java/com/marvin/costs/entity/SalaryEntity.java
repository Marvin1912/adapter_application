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

/** JPA entity representing a salary entry in the finance schema. */
@Entity
@Table(name = "salary", schema = "finance")
public class SalaryEntity extends BasicEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;

    @Basic
    @Column(name = "salary_date", nullable = false)
    private LocalDate salaryDate;

    @Basic
    @Column(name = "value", nullable = false, precision = 2)
    private BigDecimal value;

    /** Default constructor required by JPA. */
    public SalaryEntity() {
        // NOOP
    }

    /**
     * Constructs a new {@code SalaryEntity} with the given fields.
     *
     * @param salaryDate the date of the salary
     * @param value      the salary value
     */
    public SalaryEntity(LocalDate salaryDate, BigDecimal value) {
        this.salaryDate = salaryDate;
        this.value = value;
    }

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
     * Returns the salary date.
     *
     * @return the salary date
     */
    public LocalDate getSalaryDate() {
        return salaryDate;
    }

    /**
     * Sets the salary date.
     *
     * @param salaryDate the salary date to set
     */
    public void setSalaryDate(LocalDate salaryDate) {
        this.salaryDate = salaryDate;
    }

    /**
     * Returns the salary value.
     *
     * @return the value
     */
    public BigDecimal getValue() {
        return value;
    }

    /**
     * Sets the salary value.
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
        final SalaryEntity that = (SalaryEntity) o;
        return id == that.id && Objects.equals(salaryDate, that.salaryDate) && Objects.equals(value,
                that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, salaryDate, value);
    }

    @Override
    public String toString() {
        return "SalaryEntity{"
                + "id=" + id
                + ", salaryDate=" + salaryDate
                + ", value=" + value
                + '}';
    }
}
