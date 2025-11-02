package com.marvin.common.costs;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyCostDTO(LocalDate costDate, BigDecimal value, String description) {

}
