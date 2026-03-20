package com.marvin.itnews.configuration;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("FlywayConfigItNews")
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flywayItNews(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/it-news")
                .schemas("it_news")
                .table("flyway_schema_history_it_news")
                .baselineOnMigrate(true)
                .load();
    }

}
