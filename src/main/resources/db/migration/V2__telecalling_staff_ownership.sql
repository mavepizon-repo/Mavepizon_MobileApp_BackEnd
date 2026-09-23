ALTER TABLE telecalling_enquiry
    ADD COLUMN IF NOT EXISTS staff_id BIGINT;

-- Preserve ownership for enquiries that already have call history.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'telecalling_call_log'
    ) THEN
        UPDATE telecalling_enquiry e
        SET staff_id = latest.staff_id
        FROM (
            SELECT DISTINCT ON (enquiry_id)
                   enquiry_id, staff_id
            FROM telecalling_call_log
            WHERE staff_id IS NOT NULL
            ORDER BY enquiry_id, call_time DESC NULLS LAST, id DESC
        ) latest
        WHERE e.id = latest.enquiry_id
          AND e.staff_id IS NULL;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_telecalling_enquiry_staff'
    ) THEN
        ALTER TABLE telecalling_enquiry
            ADD CONSTRAINT fk_telecalling_enquiry_staff
            FOREIGN KEY (staff_id) REFERENCES office_staff(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_telecalling_enquiry_staff_id
    ON telecalling_enquiry(staff_id);
