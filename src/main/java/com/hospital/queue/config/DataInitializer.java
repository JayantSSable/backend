package com.hospital.queue.config;

import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration class for database initialization.
 * 
 * Note: Database initialization is now handled by Flyway migrations.
 * The SQL scripts are located in src/main/resources/db/migration/
 * - V1__Initial_Schema.sql: Creates the database schema
 * - V2__Sample_Data.sql: Inserts sample data
 * - V3__Set_Sequence_Values.sql: Sets sequence values
 */
@Configuration
public class DataInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    public DataInitializer() {
        logger.info("Database initialization is now handled by Flyway migrations");
    }
}
