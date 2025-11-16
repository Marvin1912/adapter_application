package com.marvin.entities.costs;

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

@Entity
@Table(name = "special_cost_entry", schema = "public", catalog = "costs")
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

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(String additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  public BigDecimal getValue() {
    return value;
  }

  public void setValue(BigDecimal value) {
    this.value = value;
  }

  public SpecialCostEntity getSpecialCost() {
    return specialCost;
  }

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
    SpecialCostEntryEntity that = (SpecialCostEntryEntity) o;
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
