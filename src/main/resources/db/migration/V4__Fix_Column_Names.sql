-- V4__Fix_Column_Names.sql
-- Fix column names to match entity model expectations

-- Make sure the contact_number column exists in hospitals table
DO $$
BEGIN
    -- Check if the column exists with a different name (contactNumber)
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'hospitals' 
        AND column_name = 'contactnumber'
    ) THEN
        -- Rename the column to match our entity definition
        ALTER TABLE hospitals RENAME COLUMN contactnumber TO contact_number;
    END IF;
    
    -- Make sure updated_at has a default value in the hospitals table
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'hospitals' 
        AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE hospitals ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;
