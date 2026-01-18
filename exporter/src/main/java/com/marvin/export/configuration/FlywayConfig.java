package com.marvin.export.configuration;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("FlywayConfigExports")
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flywayExports(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/exports")
                .schemas("exports")
                .table("flyway_schema_history_exports")
                .baselineOnMigrate(true)
                .load();
    }
}
