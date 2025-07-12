package com.marvin.plants.service;

import com.marvin.plants.dto.PlantDTO;
import com.marvin.plants.entity.Plant;
import com.marvin.plants.mapper.PlantMapper;
import com.marvin.plants.repository.PlantRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class PlantService {

    private final String mailUsername;
    private final PlantRepository plantRepository;
    private final PlantMapper plantMapper;
    private final JavaMailSender mailSender;

    public PlantService(
            @Value("${spring.mail.username}") String mailUsername,
            PlantRepository plantRepository,
            PlantMapper plantMapper,
            JavaMailSender mailSender
    ) {
        this.mailUsername = mailUsername;
        this.plantRepository = plantRepository;
        this.plantMapper = plantMapper;
        this.mailSender = mailSender;
    }

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

    // TODO: Make the watering logic cleaner (the method call does not use the return value f.e.)
    @Transactional
    public void updatePlant(PlantDTO dto) {
        plantRepository.findById(dto.id()).ifPresentOrElse(
                plant -> {
                    plantMapper.toPlant(plant, dto);
                    wateredPlant(dto.id(), dto.lastWateredDate());
                },
                () -> {
                    throw new IllegalArgumentException("Plant with id %s not found".formatted(dto.id()));
                }
        );
    }

    @Transactional
    public PlantDTO wateredPlant(long id, LocalDate lastWatered) {

        final Plant plant = plantRepository.findById(id).orElseThrow();
        plant.setLastWateredDate(lastWatered);
        plant.setNextWateredDate(lastWatered.plusDays(plant.getWateringFrequency()));

        return plantMapper.toPlantDTO(plant);
    }

    public void sendWateringNotification() {

        final LocalDate today = LocalDate.now();

        final Collection<Plant> plantsToWater = plantRepository.findByNextWateredDate(today);
        if (plantsToWater.isEmpty()) {
            return;
        }

        final String text = plantsToWater.stream()
                .map(plant -> """
                        Plant: %s, Location: %s
                        """.formatted(plant.getName(), plant.getLocation())
                )
                .collect(Collectors.joining("\n"));

        final SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailUsername);
        message.setTo(mailUsername);
        message.setSubject("Plants to water at: %s".formatted(today));
        message.setText(text);

        mailSender.send(message);
    }
}
