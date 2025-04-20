package com.marvin.database.configuration;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration(value = "FlywayConfigMain")
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flywayMain(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/costs")
                .baselineOnMigrate(true)
                .load();
    }

}
