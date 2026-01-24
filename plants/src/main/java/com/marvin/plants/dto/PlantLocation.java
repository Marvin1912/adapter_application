package com.marvin.plants.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Possible locations for a plant within the household")
public enum PlantLocation {
    LIVING_ROOM,
    BEDROOM,
    KITCHEN,
    UNDEFINED
}
