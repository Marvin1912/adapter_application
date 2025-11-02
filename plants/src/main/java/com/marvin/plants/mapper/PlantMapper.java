package com.marvin.plants.mapper;

import com.marvin.plants.dto.PlantDTO;
import com.marvin.plants.entity.Plant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PlantMapper {

  PlantDTO toPlantDTO(Plant plant);

  @Mapping(source = "imageUuid", target = "image")
  Plant toPlant(PlantDTO plantDto, String imageUuid);

  @Mapping(target = "id", ignore = true)
  void toPlant(@MappingTarget Plant plant, PlantDTO plantDto);
}
