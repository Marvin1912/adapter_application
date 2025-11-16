package com.marvin.consul.repository;

import org.springframework.stereotype.Component;

@Component
public class MonthlyCostConsulRepository extends CostConsulRepository {

    private static final String KEY_PREFIX = "costs/monthly";

    public MonthlyCostConsulRepository(BasicConsulRepository consulRepository) {
        super(consulRepository, KEY_PREFIX);
    }
}
