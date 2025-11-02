package com.marvin.image.configuration;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "FlywayConfigImages")
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flywayImages(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/images")
                .schemas("images")
                .table("flyway_schema_history_images")
                .baselineOnMigrate(true)
                .load();
    }

}
