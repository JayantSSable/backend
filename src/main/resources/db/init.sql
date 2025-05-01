-- Create database if it doesn't exist
-- Note: This needs to be run as a PostgreSQL superuser
CREATE DATABASE hospital_queue_db WITH ENCODING 'UTF8';

-- Connect to the database
\c hospital_queue_db;

-- Create schema
CREATE SCHEMA IF NOT EXISTS public;

-- Create sequences for IDs
CREATE SEQUENCE IF NOT EXISTS department_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS queue_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS patient_seq START WITH 1 INCREMENT BY 1;

-- Create tables
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS queues (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    department_id BIGINT NOT NULL,
    qr_code_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS patients (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    email VARCHAR(255),
    queue_id BIGINT NOT NULL,
    queue_position INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    registration_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (queue_id) REFERENCES queues(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_queue_department ON queues(department_id);
CREATE INDEX IF NOT EXISTS idx_patient_queue ON patients(queue_id);
CREATE INDEX IF NOT EXISTS idx_patient_status ON patients(status);
CREATE INDEX IF NOT EXISTS idx_queue_qrcode ON queues(qr_code_id);

-- Insert sample data (optional)
INSERT INTO departments (id, name, description) 
VALUES 
    (nextval('department_seq'), 'Cardiology', 'Heart and cardiovascular system'),
    (nextval('department_seq'), 'Orthopedics', 'Bones, joints, and muscles'),
    (nextval('department_seq'), 'Pediatrics', 'Children''s health')
ON CONFLICT DO NOTHING;

-- Sample queues with QR codes
INSERT INTO queues (id, name, description, department_id, qr_code_id) 
VALUES 
    (nextval('queue_seq'), 'Cardiology General', 'General cardiology consultations', 1, 'CARD001'),
    (nextval('queue_seq'), 'Cardiology Emergency', 'Urgent cardiac cases', 1, 'CARD002'),
    (nextval('queue_seq'), 'Orthopedics General', 'General orthopedic consultations', 2, 'ORTH001'),
    (nextval('queue_seq'), 'Pediatrics General', 'General pediatric consultations', 3, 'PED001')
ON CONFLICT DO NOTHING;
