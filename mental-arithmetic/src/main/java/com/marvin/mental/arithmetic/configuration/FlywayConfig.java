package com.marvin.mental.arithmetic.configuration;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flywayMentalArithmetic(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/mental-arithmetic")
                .schemas("mental_arithmetic")
                .table("flyway_schema_history_mental_arithmetic")
                .baselineOnMigrate(true)
                .load();
    }
}
