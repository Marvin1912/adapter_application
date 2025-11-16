package com.marvin.plants.configuration;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("FlywayConfigPlants")
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flywayPlants(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/plants")
                .schemas("plants")
                .table("flyway_schema_history_plants")
                .baselineOnMigrate(true)
                .load();
    }

}
