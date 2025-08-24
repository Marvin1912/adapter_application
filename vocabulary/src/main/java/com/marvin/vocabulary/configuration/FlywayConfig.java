package com.marvin.vocabulary.configuration;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration(value = "FlywayConfigVocabulary")
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flywayVocabulary(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/vocabulary")
                .schemas("vocabulary")
                .table("flyway_schema_history_vocabulary")
                .baselineOnMigrate(true)
                .load();
    }

}
