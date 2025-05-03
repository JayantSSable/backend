package com.hospital.queue.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Configuration class for logging Flyway migration details.
 * This helps diagnose migration issues in production.
 */
@Configuration
public class FlywayMigrationLogger {

    private static final Logger logger = LoggerFactory.getLogger(FlywayMigrationLogger.class);

    @Bean
    public CommandLineRunner logFlywayMigrations(Flyway flyway) {
        return args -> {
            logger.info("Checking Flyway migration status...");
            
            try {
                MigrationInfoService migrationInfoService = flyway.info();
                MigrationInfo[] migrations = migrationInfoService.all();
                
                logger.info("Database: {}", flyway.getConfiguration().getDataSource().getConnection().getMetaData().getURL());
                logger.info("Schema version: {}", migrationInfoService.current() != null ? 
                        migrationInfoService.current().getVersion().toString() : "No schema version");
                logger.info("Flyway migration locations: {}", flyway.getConfiguration().getLocations().toString());
                
                logger.info("Total migrations: {}", migrations.length);
                
                if (migrations.length > 0) {
                    logger.info("Migration details:");
                    for (MigrationInfo migration : migrations) {
                        if (migration != null) {
                            String status = migration.getState().isApplied() ? "APPLIED" : "PENDING";
                            logger.info("  {} - {} - {} - {}", 
                                    migration.getVersion(), 
                                    migration.getDescription(),
                                    status,
                                    migration.getType());
                        }
                    }
                } else {
                    logger.warn("No migrations found. Check your migration scripts location.");
                }
                
                // Log any pending migrations
                MigrationInfo[] pendingMigrations = migrationInfoService.pending();
                if (pendingMigrations.length > 0) {
                    logger.info("Pending migrations:");
                    for (MigrationInfo migration : pendingMigrations) {
                        logger.info("  {} - {}", migration.getVersion(), migration.getDescription());
                    }
                }
            } catch (Exception e) {
                logger.error("Error checking Flyway migration status", e);
            }
        };
    }
}
