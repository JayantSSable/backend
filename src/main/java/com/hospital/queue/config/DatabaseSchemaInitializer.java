package com.hospital.queue.config;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * This class ensures that the database schema includes all necessary tables and columns
 * for the multi-hospital functionality, even when using 'update' mode.
 * 
 * It runs SQL commands to check for and create missing tables/columns without affecting existing data.
 */
@Configuration
public class DatabaseSchemaInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaInitializer.class);
    
    @Autowired
    private DataSource dataSource;
    
    @PostConstruct
    public void initializeSchema() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        try {
            logger.info("Checking and initializing database schema for multi-hospital support...");
            
            // Check if hospitals table exists
            boolean hospitalsTableExists = tableExists(jdbcTemplate, "hospitals");
            if (!hospitalsTableExists) {
                logger.info("Creating hospitals table...");
                jdbcTemplate.execute(
                    "CREATE TABLE hospitals (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "description TEXT, " +
                    "created_at TIMESTAMP, " +
                    "updated_at TIMESTAMP" +
                    ")"
                );
                logger.info("Hospitals table created successfully");
            } else {
                logger.info("Hospitals table already exists");
            }
            
            // Check if departments table has hospital_id column
            boolean hospitalIdColumnExists = columnExists(jdbcTemplate, "departments", "hospital_id");
            if (!hospitalIdColumnExists) {
                logger.info("Adding hospital_id column to departments table...");
                
                // First create a default hospital if none exists
                if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hospitals", Integer.class) == 0) {
                    jdbcTemplate.update(
                        "INSERT INTO hospitals (name, description, created_at, updated_at) VALUES (?, ?, NOW(), NOW())",
                        "Default Hospital", "Default hospital created during schema migration"
                    );
                    logger.info("Created default hospital");
                }
                
                // Get the ID of the first hospital to use as default
                Integer defaultHospitalId = jdbcTemplate.queryForObject(
                    "SELECT id FROM hospitals ORDER BY id LIMIT 1", 
                    Integer.class
                );
                
                // Add hospital_id column with default value
                jdbcTemplate.execute(
                    "ALTER TABLE departments ADD COLUMN hospital_id INTEGER"
                );
                
                // Set default value for existing departments
                jdbcTemplate.update(
                    "UPDATE departments SET hospital_id = ?", 
                    defaultHospitalId
                );
                
                // Add foreign key constraint
                jdbcTemplate.execute(
                    "ALTER TABLE departments " +
                    "ADD CONSTRAINT fk_departments_hospital " +
                    "FOREIGN KEY (hospital_id) REFERENCES hospitals(id)"
                );
                
                // Make hospital_id not nullable
                jdbcTemplate.execute(
                    "ALTER TABLE departments ALTER COLUMN hospital_id SET NOT NULL"
                );
                
                logger.info("Added hospital_id column to departments table and linked existing departments to default hospital");
            } else {
                logger.info("Hospital_id column already exists in departments table");
            }
            
            logger.info("Database schema initialization completed successfully");
            
        } catch (Exception e) {
            logger.error("Error initializing database schema: " + e.getMessage(), e);
            // Don't throw exception - let the application continue to start
            // Hibernate will handle any remaining schema issues
        }
    }
    
    private boolean tableExists(JdbcTemplate jdbcTemplate, String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ? AND table_schema = 'public'",
                Integer.class,
                tableName
            );
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("Error checking if table exists: " + e.getMessage(), e);
            return false;
        }
    }
    
    private boolean columnExists(JdbcTemplate jdbcTemplate, String tableName, String columnName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = ? AND column_name = ? AND table_schema = 'public'",
                Integer.class,
                tableName,
                columnName
            );
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("Error checking if column exists: " + e.getMessage(), e);
            return false;
        }
    }
}
