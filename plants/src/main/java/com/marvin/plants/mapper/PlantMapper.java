package com.marvin.plants.mapper;

import com.marvin.plants.dto.PlantDTO;
import com.marvin.plants.entity.Plant;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlantMapper {

    PlantDTO toPlantDTO(Plant plant);

    Plant toPlant(PlantDTO plantDTO);

}
