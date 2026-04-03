package com.marvin.costs.infrastructure;

import com.marvin.consul.repository.SalaryConsulRepository;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Consul-backed implementation of {@link Ibans} that provides IBANs for salary imports. */
@Component("salaryImportIbans")
public class SalaryImportIbansConsul implements Ibans {

    private static final Logger LOGGER = LoggerFactory.getLogger(SalaryImportIbansConsul.class);

    private final Set<String> salaryIbans;
    private final SalaryConsulRepository salaryConsulRepository;

    /**
     * Constructs a new {@code SalaryImportIbansConsul} and initialises the salary IBANs.
     *
     * @param salaryConsulRepository the Consul repository for salary configuration
     */
    public SalaryImportIbansConsul(SalaryConsulRepository salaryConsulRepository) {
        this.salaryConsulRepository = salaryConsulRepository;
        this.salaryIbans = initIbans();
    }

    private Set<String> initIbans() {
        final String property = salaryConsulRepository.getProperty("iban/import");
        final Set<String> ibans = Set.of(property.split(","));
        LOGGER.info("Initialized salary import IBANs with: {}!", ibans);
        return ibans;
    }

    @Override
    public Set<String> getIbans() {
        return salaryIbans;
    }
}
