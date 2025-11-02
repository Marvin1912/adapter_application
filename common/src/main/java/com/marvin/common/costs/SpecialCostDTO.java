package com.marvin.common.costs;

import java.time.LocalDate;
import java.util.List;

public record SpecialCostDTO(LocalDate costDate, List<SpecialCostEntryDTO> entries) {

}
