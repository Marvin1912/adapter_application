package com.marvin.influxdb.costs;

public enum CostType {

  DAILY("daily"),
  MONTHLY("monthly"),
  SALARY("salary");

  private final String value;

  CostType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
