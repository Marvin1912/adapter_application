package com.marvin.plants.configuration;

import com.marvin.plants.service.PlantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
public class PlantSchedulerConfig {

    private final PlantService plantService;

    public PlantSchedulerConfig(PlantService plantService) {
        this.plantService = plantService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        executeWateringSchedule();
        executeFertilizingSchedule();
    }

    @Scheduled(cron = "0 0 0/4 * * *")
    public void scheduleWatering() {
        executeWateringSchedule();
    }

    @Scheduled(cron = "0 5 0 * * *")
    public void scheduleFertilizing() {
        executeFertilizingSchedule();
    }

    private void executeWateringSchedule() {
        log.info("Scheduling watering");
        plantService.sendWateringNotification();
    }

    private void executeFertilizingSchedule() {
        log.info("Scheduling Fertilizing");
        plantService.sendFertilizingNotification();
    }

}
