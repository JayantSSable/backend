-- V2__Sample_Data.sql
-- Sample data for hospital queue system

-- Insert sample hospital
INSERT INTO hospitals (name, address, contact_number, email, description, created_at, updated_at)
VALUES 
    ('General Hospital', '123 Main Street, City', '+1234567890', 'contact@generalhospital.com', 'A leading healthcare facility providing comprehensive medical services', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample departments
INSERT INTO departments (name, description, hospital_id, created_at, updated_at) 
VALUES 
    ('Cardiology', 'Heart and cardiovascular system', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Orthopedics', 'Bones, joints, and muscles', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Pediatrics', 'Children''s health', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample queues with QR codes
INSERT INTO queues (name, description, department_id, qr_code_id, created_at, updated_at) 
VALUES 
    ('Cardiology General', 'General cardiology consultations', 1, 'CARD001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Cardiology Emergency', 'Urgent cardiac cases', 1, 'CARD002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Orthopedics General', 'General orthopedic consultations', 2, 'ORTH001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Pediatrics General', 'General pediatric consultations', 3, 'PED001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
