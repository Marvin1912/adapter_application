package com.marvin.consul.repository;

import org.springframework.stereotype.Component;

@Component
public class SalaryConsulRepository extends CostConsulRepository {

    private static final String KEY_PREFIX = "costs/salary";

    public SalaryConsulRepository(BasicConsulRepository consulRepository) {
        super(consulRepository, KEY_PREFIX);
    }
}
