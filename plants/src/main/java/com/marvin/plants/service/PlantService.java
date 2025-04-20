package com.marvin.plants.service;

import com.marvin.plants.dto.PlantDTO;
import com.marvin.plants.entity.Plant;
import com.marvin.plants.mapper.PlantMapper;
import com.marvin.plants.repository.PlantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Service
public class PlantService {

    private final PlantRepository plantRepository;
    private final PlantMapper plantMapper;

    public PlantService(PlantRepository plantRepository, PlantMapper plantMapper) {
        this.plantRepository = plantRepository;
        this.plantMapper = plantMapper;
    }

    public long createPlant(PlantDTO plantDto) {
        return plantRepository.save(plantMapper.toPlant(plantDto)).getId();
    }

    public PlantDTO getPlant(long id) {
        Plant plant = plantRepository.findById(id).orElse(null);
        return plantMapper.toPlantDTO(plant);
    }

    public Flux<PlantDTO> getPlants() {
        return Flux.fromIterable(plantRepository.findAll()).map(plantMapper::toPlantDTO);
    }

    @Transactional
    public void uploadImage(long id, byte[] image) {
        final Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Plant with id %s not found".formatted(id)));
        plant.setImage(image);
    }
}
