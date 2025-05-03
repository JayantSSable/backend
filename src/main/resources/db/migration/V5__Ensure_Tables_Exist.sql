-- V5__Ensure_Tables_Exist.sql
-- This migration ensures that all required tables exist in the database
-- It will create any missing tables with the correct structure

-- Create hospitals table if it doesn't exist
CREATE TABLE IF NOT EXISTS hospitals (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    contact_number VARCHAR(20),
    email VARCHAR(255),
    description VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create departments table if it doesn't exist
CREATE TABLE IF NOT EXISTS departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    hospital_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_departments_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id) ON DELETE CASCADE
);

-- Create queues table if it doesn't exist
CREATE TABLE IF NOT EXISTS queues (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    department_id BIGINT NOT NULL,
    qr_code_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_queues_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
);

-- Create patients table if it doesn't exist
CREATE TABLE IF NOT EXISTS patients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    email VARCHAR(255),
    queue_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    queue_position INTEGER,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    served_at TIMESTAMP,
    notified_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_patients_queue FOREIGN KEY (queue_id) REFERENCES queues(id) ON DELETE CASCADE
);

-- Create patient_devices table if it doesn't exist
CREATE TABLE IF NOT EXISTS patient_devices (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    device_token VARCHAR(512) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_patient_devices_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Create indexes if they don't exist
CREATE INDEX IF NOT EXISTS idx_department_hospital ON departments(hospital_id);
CREATE INDEX IF NOT EXISTS idx_queue_department ON queues(department_id);
CREATE INDEX IF NOT EXISTS idx_patient_queue ON patients(queue_id);
CREATE INDEX IF NOT EXISTS idx_patient_status ON patients(status);
CREATE INDEX IF NOT EXISTS idx_queue_qrcode ON queues(qr_code_id);
CREATE INDEX IF NOT EXISTS idx_patient_device_patient ON patient_devices(patient_id);
CREATE INDEX IF NOT EXISTS idx_patient_device_token ON patient_devices(device_token);
