-- V3__Set_Sequence_Values.sql
-- Set sequence values to continue after our sample data

-- When using BIGSERIAL, PostgreSQL automatically creates sequences with names like tablename_id_seq
-- This ensures the sequences continue from the highest ID values
DO $$ 
BEGIN
  -- Only set sequence values if the tables have data
  IF EXISTS (SELECT 1 FROM hospitals) THEN
    PERFORM setval('hospitals_id_seq', (SELECT MAX(id) FROM hospitals), true);
  END IF;
  
  IF EXISTS (SELECT 1 FROM departments) THEN
    PERFORM setval('departments_id_seq', (SELECT MAX(id) FROM departments), true);
  END IF;
  
  IF EXISTS (SELECT 1 FROM queues) THEN
    PERFORM setval('queues_id_seq', (SELECT MAX(id) FROM queues), true);
  END IF;
  
  IF EXISTS (SELECT 1 FROM patients) THEN
    PERFORM setval('patients_id_seq', (SELECT MAX(id) FROM patients), true);
  END IF;
  
  IF EXISTS (SELECT 1 FROM patient_devices) THEN
    PERFORM setval('patient_devices_id_seq', (SELECT MAX(id) FROM patient_devices), true);
  END IF;
END $$;
