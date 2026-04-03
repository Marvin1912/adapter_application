package com.marvin.costs.infrastructure;

import com.marvin.consul.repository.MonthlyCostConsulRepository;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Consul-backed implementation of {@link Ibans} that provides blocked IBANs for monthly costs. */
@Component("monthlyCostBlockedIbans")
public class MonthlyCostBlockedIbansConsul implements Ibans {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            MonthlyCostBlockedIbansConsul.class);

    private final Set<String> blockedIbans;
    private final MonthlyCostConsulRepository monthlyCostConsulRepository;

    /**
     * Constructs a new {@code MonthlyCostBlockedIbansConsul} and initialises the blocked IBANs.
     *
     * @param monthlyCostConsulRepository the Consul repository for monthly cost configuration
     */
    public MonthlyCostBlockedIbansConsul(MonthlyCostConsulRepository monthlyCostConsulRepository) {
        this.monthlyCostConsulRepository = monthlyCostConsulRepository;
        this.blockedIbans = initIbans();
    }

    private Set<String> initIbans() {
        final String property = monthlyCostConsulRepository.getProperty("iban/blocked");
        final Set<String> ibans = Set.of(property.split(","));
        LOGGER.info("Initialized monthly cost blocked IBANs with: {}!", ibans);
        return ibans;
    }

    @Override
    public Set<String> getIbans() {
        return blockedIbans;
    }
}
