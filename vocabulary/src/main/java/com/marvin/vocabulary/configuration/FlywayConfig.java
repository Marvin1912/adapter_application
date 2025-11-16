package com.marvin.vocabulary.configuration;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("FlywayConfigVocabulary")
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
