-- ============================================================
-- V17: Fix incorrect BCrypt hash for seed users from V16
--
-- The hash in V16 ($2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)
-- does NOT match the password "Demo@2026" — verified via bcrypt.checkpw().
-- This migration replaces it with a verified hash (strength 10, prefix $2a$).
--
-- Password for all three users: Demo@2026
-- ============================================================

UPDATE users
SET password_hash = '$2a$10$B6m3U6XOAUahJ03W.fqwv.H6BbAAN3InhXZUeMqHr4c1DSRDNbgPS',
    updated_at    = NOW()
WHERE email IN (
    'operador@freightflow.io',
    'viewer@freightflow.io',
    'cliente@atlascargo.com'
);
