package com.marvin.backup.configuration;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Flyway configuration for the backup (exports) schema migrations. */
@Configuration("BackupFlywayConfig")
public class BackupFlywayConfig {

    /**
     * Configures and runs Flyway migrations for the exports (backup) schema.
     *
     * @param dataSource the data source to use for migrations
     * @return the configured Flyway instance
     */
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
