package com.marvin.plants.configuration;

import com.marvin.plants.service.PlantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
public class SchedulerConfig {

  private final PlantService plantService;

  public SchedulerConfig(PlantService plantService) {
    this.plantService = plantService;
  }

  @Scheduled(cron = "0 0 2 * * *")
  public void scheduleWatering() {
    log.info("Scheduling watering");
    plantService.sendWateringNotification();
  }

}
