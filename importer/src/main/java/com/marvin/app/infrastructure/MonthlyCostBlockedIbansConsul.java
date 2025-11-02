package com.marvin.app.infrastructure;

import com.marvin.consul.repository.MonthlyCostConsulRepository;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("monthlyCostBlockedIbans")
public class MonthlyCostBlockedIbansConsul implements Ibans {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      MonthlyCostBlockedIbansConsul.class);

  private final Set<String> blockedIbans;
  private final MonthlyCostConsulRepository monthlyCostConsulRepository;

  public MonthlyCostBlockedIbansConsul(MonthlyCostConsulRepository monthlyCostConsulRepository) {
    this.monthlyCostConsulRepository = monthlyCostConsulRepository;
    this.blockedIbans = initIbans();
  }

  private Set<String> initIbans() {

    String property = monthlyCostConsulRepository.getProperty("iban/blocked");

    Set<String> blockedIbans = Set.of(property.split(","));

    LOGGER.info("Initialized monthly cost blocked IBANs with: {}!", blockedIbans);

    return blockedIbans;
  }

  @Override
  public Set<String> getIbans() {
    return blockedIbans;
  }
}
