package com.marvin.plants.service;

import com.marvin.plants.dto.PlantDTO;
import com.marvin.plants.entity.Plant;
import com.marvin.plants.mapper.PlantMapper;
import com.marvin.plants.repository.PlantRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Service
public class PlantService {

    private final PlantRepository plantRepository;
    private final PlantMapper plantMapper;
    private final MeterRegistry meterRegistry;
    private final Map<Integer, AtomicInteger> wateringStates = new ConcurrentHashMap<>();

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
                    fertilizePlant(plant, dto.lastFertilizedDate());
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

    @Transactional
    public PlantDTO fertilizePlant(long id, LocalDate lastFertilized) {

        final Plant plant = plantRepository.findById(id).orElseThrow();
        fertilizePlant(plant, lastFertilized);

        return plantMapper.toPlantDTO(plant);
    }

    private void waterPlant(Plant plant, LocalDate lastWatered) {
        plant.setLastWateredDate(lastWatered);
        plant.setNextWateredDate(lastWatered.plusDays(plant.getWateringFrequency()));
    }

    private void fertilizePlant(Plant plant, LocalDate lastFertilized) {
        if (lastFertilized != null && plant.getFertilizingFrequency() != null) {
            plant.setLastFertilizedDate(lastFertilized);
            LocalDate nextFertilized = lastFertilized.plusDays(plant.getFertilizingFrequency());

            // Fertilizing period is April (month 4) to October (month 10)
            // If next fertilizing date is outside this range, set to April 15th
            int month = nextFertilized.getMonthValue();
            if (month < 4 || month > 10) {
                int year = nextFertilized.getYear();
                // If current month is October, November, or December, increment year
                if (month >= 10) {
                    year++;
                }
                nextFertilized = LocalDate.of(year, 4, 15);
            }

            plant.setNextFertilizedDate(nextFertilized);
        }
    }

    public void sendWateringNotification() {
        final LocalDate today = LocalDate.now();
        plantRepository.findAll().forEach(plant ->
                wateringStates.get(plant.getId()).set(plant.getNextWateredDate().isEqual(today) ? 1 : 0)
        );
    }
}
