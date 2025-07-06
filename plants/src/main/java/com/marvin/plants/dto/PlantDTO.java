package com.marvin.plants.dto;

import java.time.LocalDateTime;

public record PlantDTO(
        long id,
        String name,
        String species,
        String description,
        String careInstructions,
        PlantLocation location,
        Integer wateringFrequency,
        LocalDateTime lastWateredDate,
        LocalDateTime nextWateredDate,
        String image
) {
}
