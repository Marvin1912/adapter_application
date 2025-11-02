package com.marvin.common.costs;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MonthlyCostDTO(LocalDate costDate, BigDecimal value) {

}
