package com.finsightanalytics.recommendationengine.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4jConfig {

    @Value("${neo4j.uri}")
    private String uri;

    @Value("${neo4j.username}")
    private String username;

    @Value("${neo4j.password}")
    private String password;

    @Value("${neo4j.database:neo4j}")
    private String database;

    @Bean
    public Driver neo4jDriver() {
        Config config = Config.builder()
                .withLogging(org.neo4j.driver.Logging.slf4j())
                .build();
        return GraphDatabase.driver(uri, AuthTokens.basic(username, password), config);
    }

    @Bean
    public SessionConfig sessionConfig() {
        return SessionConfig.forDatabase(database);
    }
}
