package com.marvin.database.configuration;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("FlywayConfigMain")
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flywayMain(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/costs")
                .schemas("finance")
                .baselineOnMigrate(true)
                .load();
    }

    @Bean(initMethod = "migrate")
    public Flyway flywayExports(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/exports")
                .schemas("exports")
                .baselineOnMigrate(true)
                .baselineVersion("1.1")
                .load();
    }

}
