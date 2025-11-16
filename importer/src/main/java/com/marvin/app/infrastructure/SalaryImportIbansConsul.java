package com.marvin.app.infrastructure;

import com.marvin.consul.repository.SalaryConsulRepository;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("salaryImportIbans")
public class SalaryImportIbansConsul implements Ibans {

    private static final Logger LOGGER = LoggerFactory.getLogger(SalaryImportIbansConsul.class);

    private final Set<String> salaryIbans;
    private final SalaryConsulRepository salaryConsulRepository;

    public SalaryImportIbansConsul(SalaryConsulRepository salaryConsulRepository) {
        this.salaryConsulRepository = salaryConsulRepository;
        this.salaryIbans = initIbans();
    }

    private Set<String> initIbans() {

        String property = salaryConsulRepository.getProperty("iban/import");

        Set<String> salaryIbans = Set.of(property.split(","));

        LOGGER.info("Initialized salary import IBANs with: {}!", salaryIbans);

        return salaryIbans;
    }

    @Override
    public Set<String> getIbans() {
        return salaryIbans;
    }
}
