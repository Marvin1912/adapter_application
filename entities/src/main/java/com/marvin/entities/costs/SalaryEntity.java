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

    public SalaryEntity() {
        // NOOP
    }

    public SalaryEntity(LocalDate salaryDate, BigDecimal value) {
        this.salaryDate = salaryDate;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getSalaryDate() {
        return salaryDate;
    }

    public void setSalaryDate(LocalDate salaryDate) {
        this.salaryDate = salaryDate;
    }

    public BigDecimal getValue() {
        return value;
    }

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
        SalaryEntity that = (SalaryEntity) o;
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
