-- V19: Add POSITION_UPDATE to the events.type CHECK constraint.
--
-- The events table uses VARCHAR(50) + a CHECK constraint (not a PostgreSQL enum type).
-- The constraint was created inline in V6 without an explicit name, so PostgreSQL
-- auto-generated the name.  We use pg_constraint to find and drop it dynamically,
-- then recreate it with POSITION_UPDATE included.
--
-- If the constraint had been a PostgreSQL enum (CREATE TYPE event_type AS ENUM …)
-- we would have used: ALTER TYPE event_type ADD VALUE IF NOT EXISTS 'POSITION_UPDATE';
-- That is NOT the case here — the column is plain VARCHAR(50).

DO $$
DECLARE
    v_constraint_name TEXT;
BEGIN
    -- Find the CHECK constraint on the events table that covers the event type values.
    -- We match on GATE_IN which only appears in the type check.
    SELECT conname INTO v_constraint_name
    FROM pg_constraint
    WHERE conrelid = 'events'::regclass
      AND contype   = 'c'
      AND pg_get_constraintdef(oid) LIKE '%GATE_IN%';

    IF v_constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE events DROP CONSTRAINT %I', v_constraint_name);
    END IF;
END $$;

ALTER TABLE events
    ADD CONSTRAINT events_type_check
    CHECK (type IN (
        'GATE_IN',
        'LOADED',
        'DEPARTED',
        'TRANSSHIPMENT',
        'ARRIVED',
        'GATE_OUT',
        'CUSTOMS_HOLD',
        'CUSTOMS_RELEASE',
        'POSITION_UPDATE'
    ));
