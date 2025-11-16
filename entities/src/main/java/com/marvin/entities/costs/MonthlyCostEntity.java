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
@Table(name = "monthly_cost", schema = "public", catalog = "costs")
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

  public MonthlyCostEntity() {
    // NOOP
  }

  public MonthlyCostEntity(LocalDate costDate, BigDecimal value) {
    this.costDate = costDate;
    this.value = value;
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
    MonthlyCostEntity that = (MonthlyCostEntity) o;
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
