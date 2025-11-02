package com.marvin.app.infrastructure;

import com.marvin.consul.repository.SpecialCostConsulRepository;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("specialCostBlockedIbans")
public class SpecialCostBlockedIbansConsul implements Ibans {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      SpecialCostBlockedIbansConsul.class);

  private final Set<String> blockedIbans;
  private final SpecialCostConsulRepository specialCostConsulRepository;

  public SpecialCostBlockedIbansConsul(SpecialCostConsulRepository specialCostConsulRepository) {
    this.specialCostConsulRepository = specialCostConsulRepository;
    this.blockedIbans = initIbans();
  }

  private Set<String> initIbans() {
    String property = specialCostConsulRepository.getProperty("iban/blocked");

    Set<String> blockedIbans = Set.of(property.split(","));

    LOGGER.info("Initialized special cost blocked IBANs with: {}!", blockedIbans);

    return blockedIbans;
  }

  @Override
  public Set<String> getIbans() {
    return blockedIbans;
  }
}
