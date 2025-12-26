package com.marvin.plants.dto;

import java.time.LocalDate;

public record PlantDTO(
        long id,
        String name,
        String species,
        String description,
        String careInstructions,
        PlantLocation location,
        Integer wateringFrequency,
        LocalDate lastWateredDate,
        LocalDate nextWateredDate,
        String image,
        Integer fertilizingFrequency,
        LocalDate lastFertilizedDate,
        LocalDate nextFertilizedDate
) {

}
