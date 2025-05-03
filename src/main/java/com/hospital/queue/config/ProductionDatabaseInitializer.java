package com.hospital.queue.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Fallback database initializer for production environment.
 * This ensures that all required tables are created even if Flyway migrations fail.
 */
@Configuration
@Profile("prod")
public class ProductionDatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ProductionDatabaseInitializer.class);
    
    @Autowired
    private DataSource dataSource;
    
    @Bean
    public CommandLineRunner ensureDatabaseTablesExist() {
        return args -> {
            logger.info("Checking database tables in production environment...");
            
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                
                // Get all existing tables
                List<String> existingTables = new ArrayList<>();
                try (ResultSet tables = metaData.getTables(null, "public", "%", new String[]{"TABLE"})) {
                    while (tables.next()) {
                        existingTables.add(tables.getString("TABLE_NAME").toLowerCase());
                    }
                }
                
                logger.info("Existing tables: {}", existingTables);
                
                // Check if required tables exist
                List<String> requiredTables = List.of(
                        "hospitals", "departments", "queues", "patients", "patient_devices", "flyway_schema_history");
                
                List<String> missingTables = new ArrayList<>();
                for (String table : requiredTables) {
                    if (!existingTables.contains(table)) {
                        missingTables.add(table);
                    }
                }
                
                if (!missingTables.isEmpty()) {
                    logger.warn("Missing tables detected: {}", missingTables);
                    createMissingTables(missingTables);
                } else {
                    logger.info("All required tables exist in the database.");
                }
            } catch (SQLException e) {
                logger.error("Error checking database tables", e);
            }
        };
    }
    
    private void createMissingTables(List<String> missingTables) {
        logger.info("Creating missing tables: {}", missingTables);
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        // Create tables in the correct order to respect foreign key constraints
        if (missingTables.contains("hospitals")) {
            logger.info("Creating hospitals table");
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS hospitals (" +
                "    id BIGSERIAL PRIMARY KEY," +
                "    name VARCHAR(255) NOT NULL," +
                "    address VARCHAR(255)," +
                "    contact_number VARCHAR(20)," +
                "    email VARCHAR(255)," +
                "    description VARCHAR(1000)," +
                "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");
        }
        
        if (missingTables.contains("departments")) {
            logger.info("Creating departments table");
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS departments (" +
                "    id BIGSERIAL PRIMARY KEY," +
                "    name VARCHAR(255) NOT NULL," +
                "    description TEXT," +
                "    hospital_id BIGINT NOT NULL," +
                "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    CONSTRAINT fk_departments_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id) ON DELETE CASCADE" +
                ")");
        }
        
        if (missingTables.contains("queues")) {
            logger.info("Creating queues table");
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS queues (" +
                "    id BIGSERIAL PRIMARY KEY," +
                "    name VARCHAR(255) NOT NULL," +
                "    description TEXT," +
                "    department_id BIGINT NOT NULL," +
                "    qr_code_id VARCHAR(255) UNIQUE," +
                "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    CONSTRAINT fk_queues_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE" +
                ")");
        }
        
        if (missingTables.contains("patients")) {
            logger.info("Creating patients table");
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS patients (" +
                "    id BIGSERIAL PRIMARY KEY," +
                "    name VARCHAR(255) NOT NULL," +
                "    phone_number VARCHAR(20)," +
                "    email VARCHAR(255)," +
                "    queue_id BIGINT NOT NULL," +
                "    status VARCHAR(20) NOT NULL," +
                "    queue_position INTEGER," +
                "    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "    served_at TIMESTAMP," +
                "    notified_at TIMESTAMP," +
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    CONSTRAINT fk_patients_queue FOREIGN KEY (queue_id) REFERENCES queues(id) ON DELETE CASCADE" +
                ")");
        }
        
        if (missingTables.contains("patient_devices")) {
            logger.info("Creating patient_devices table");
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS patient_devices (" +
                "    id BIGSERIAL PRIMARY KEY," +
                "    patient_id BIGINT NOT NULL," +
                "    device_token VARCHAR(512) NOT NULL," +
                "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "    CONSTRAINT fk_patient_devices_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE" +
                ")");
        }
        
        // Create indexes
        logger.info("Creating indexes");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_department_hospital ON departments(hospital_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_queue_department ON queues(department_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_patient_queue ON patients(queue_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_patient_status ON patients(status)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_queue_qrcode ON queues(qr_code_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_patient_device_patient ON patient_devices(patient_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_patient_device_token ON patient_devices(device_token)");
        
        logger.info("All missing tables and indexes have been created.");
    }
}
