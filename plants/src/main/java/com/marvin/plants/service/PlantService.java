package com.marvin.plants.service;

import com.marvin.plants.dto.PlantDTO;
import com.marvin.plants.entity.Plant;
import com.marvin.plants.mapper.PlantMapper;
import com.marvin.plants.repository.PlantRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.annotation.PostConstruct;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Service
public class PlantService {

    private final PlantRepository plantRepository;
    private final PlantMapper plantMapper;
    private final MeterRegistry meterRegistry;
    private final Map<Long, AtomicInteger> wateringStates = new ConcurrentHashMap<>();

    public PlantService(
            PlantRepository plantRepository,
            PlantMapper plantMapper,
            MeterRegistry meterRegistry
    ) {
        this.plantRepository = plantRepository;
        this.plantMapper = plantMapper;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void initGauges() {
        plantRepository.findAll().forEach(plant -> {
            AtomicInteger state = wateringStates.computeIfAbsent(plant.getId(), id -> new AtomicInteger(0));
            Gauge.builder("water_plant", state, AtomicInteger::get)
                    .tag("plant", plant.getName())
                    .register(meterRegistry);
        });
    }

    @Transactional
    public long createPlant(PlantDTO plantDto, String imageUuid) {
        return plantRepository.save(plantMapper.toPlant(plantDto, imageUuid)).getId();
    }

    public PlantDTO getPlant(long id) {
        final Plant plant = plantRepository.findById(id).orElse(null);
        return plantMapper.toPlantDTO(plant);
    }

    public Flux<PlantDTO> getPlants() {
        return Flux.fromIterable(plantRepository.findAll()).map(plantMapper::toPlantDTO);
    }

    public void deletePlant(long id) {
        plantRepository.deleteById(id);
    }

    @Transactional
    public void updatePlant(PlantDTO dto) {
        plantRepository.findById(dto.id()).ifPresentOrElse(
                plant -> {
                    plantMapper.toPlant(plant, dto);
                    waterPlant(plant, dto.lastWateredDate());
                },
                () -> {
                    throw new IllegalArgumentException(
                            "Plant with id %s not found".formatted(dto.id()));
                }
        );
    }

    @Transactional
    public PlantDTO waterPlant(long id, LocalDate lastWatered) {

        final Plant plant = plantRepository.findById(id).orElseThrow();
        waterPlant(plant, lastWatered);

        return plantMapper.toPlantDTO(plant);
    }

    private void waterPlant(Plant plant, LocalDate lastWatered) {
        plant.setLastWateredDate(lastWatered);
        plant.setNextWateredDate(lastWatered.plusDays(plant.getWateringFrequency()));
    }

    public void sendWateringNotification() {
        final LocalDate today = LocalDate.now();
        plantRepository.findAll().forEach(plant ->
                wateringStates.get(plant.getId()).set(plant.getNextWateredDate().isEqual(today) ? 1 : 0)
        );
    }
}
