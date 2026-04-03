package com.marvin.costs.configuration;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Flyway configuration for the costs schema migrations. */
@Configuration("CostsFlywayConfig")
public class CostsFlywayConfig {

    /**
     * Configures and runs Flyway migrations for the costs (finance) schema.
     *
     * @param dataSource the data source to use for migrations
     * @return the configured Flyway instance
     */
    @Bean(initMethod = "migrate")
    public Flyway flywayMain(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/costs")
                .schemas("finance")
                .baselineOnMigrate(true)
                .load();
    }

}
