package com.marvin.costs.infrastructure;

import com.marvin.consul.repository.SpecialCostConsulRepository;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Consul-backed implementation of {@link Ibans} that provides blocked IBANs for special costs. */
@Component("specialCostBlockedIbans")
public class SpecialCostBlockedIbansConsul implements Ibans {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            SpecialCostBlockedIbansConsul.class);

    private final Set<String> blockedIbans;
    private final SpecialCostConsulRepository specialCostConsulRepository;

    /**
     * Constructs a new {@code SpecialCostBlockedIbansConsul} and initialises the blocked IBANs.
     *
     * @param specialCostConsulRepository the Consul repository for special cost configuration
     */
    public SpecialCostBlockedIbansConsul(SpecialCostConsulRepository specialCostConsulRepository) {
        this.specialCostConsulRepository = specialCostConsulRepository;
        this.blockedIbans = initIbans();
    }

    private Set<String> initIbans() {
        final String property = specialCostConsulRepository.getProperty("iban/blocked");
        final Set<String> ibans = Set.of(property.split(","));
        LOGGER.info("Initialized special cost blocked IBANs with: {}!", ibans);
        return ibans;
    }

    @Override
    public Set<String> getIbans() {
        return blockedIbans;
    }
}
