package com.marvin.consul.repository;

import com.marvin.consul.model.ConsulKeyValueDTO;
import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;

public abstract class CostConsulRepository implements ConsulRepository {

  private static final Logger LOGGER = Logger.getLogger(CostConsulRepository.class.getName());
  private final BasicConsulRepository consulRepository;
  private final String keyPrefix;
  protected Map<String, String> properties;

  public CostConsulRepository(BasicConsulRepository consulRepository, String keyPrefix) {
    this.consulRepository = consulRepository;
    this.keyPrefix = keyPrefix;
  }

  @PostConstruct
  @Scheduled(cron = "0 * * * * ?")
  public void init() {
    try {
      this.properties = consulRepository.getPropertiesRecursively(keyPrefix)
          .stream()
          .filter(dto -> dto != null && dto.key() != null && dto.value() != null)
          .collect(Collectors.toMap(ConsulKeyValueDTO::key, ConsulKeyValueDTO::value));
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error during initialization!!!", e);
      this.properties = Collections.emptyMap();
    }
  }

  @Override
  public String getProperty(String key) {
    final String value = properties.get(keyPrefix + "/" + key);
    return StringUtils.isBlank(value) ? StringUtils.EMPTY : value;
  }
}
