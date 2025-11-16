package com.marvin.consul.repository;

import org.springframework.stereotype.Component;

@Component
public class SpecialCostConsulRepository extends CostConsulRepository {

    private static final String KEY_PREFIX = "costs/special";

    public SpecialCostConsulRepository(BasicConsulRepository consulRepository) {
        super(consulRepository, KEY_PREFIX);
    }
}
