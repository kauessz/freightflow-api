-- ============================================================
-- V11: Seed vessels and voyages for development/testing
-- ============================================================

-- ==================== Vessels ====================
INSERT INTO vessels (id, imo, name, flag, type, capacity_teu, created_at, updated_at) VALUES
('a0000001-0000-0000-0000-000000000001', '9839012', 'MSC Oscar', 'PA', 'CONTAINER', 19224, NOW(), NOW()),
('a0000001-0000-0000-0000-000000000002', '9619907', 'CMA CGM Jacques Saade', 'FR', 'CONTAINER', 23112, NOW(), NOW()),
('a0000001-0000-0000-0000-000000000003', '9461867', 'Cap San Diego', 'LR', 'CONTAINER', 9600, NOW(), NOW()),
('a0000001-0000-0000-0000-000000000004', '9811000', 'Maersk Edmonton', 'DK', 'CONTAINER', 15226, NOW(), NOW()),
('a0000001-0000-0000-0000-000000000005', '9786744', 'Nave Andromeda', 'MH', 'TANKER', 0, NOW(), NOW());

-- ==================== Voyages ====================
-- Santos → Rotterdam (MSC Oscar)
INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT
    'b0000001-0000-0000-0000-000000000001',
    'MSC-2026-001',
    'a0000001-0000-0000-0000-000000000001',
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM ports WHERE unlocode = 'NLRTM'),
    NOW() + INTERVAL '3 days',
    NOW() + INTERVAL '21 days',
    'SCHEDULED',
    NOW(), NOW();

-- Rotterdam → Shanghai (CMA CGM Jacques Saade)
INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT
    'b0000001-0000-0000-0000-000000000002',
    'CMA-2026-042',
    'a0000001-0000-0000-0000-000000000002',
    (SELECT id FROM ports WHERE unlocode = 'NLRTM'),
    (SELECT id FROM ports WHERE unlocode = 'CNSHA'),
    NOW() + INTERVAL '5 days',
    NOW() + INTERVAL '35 days',
    'SCHEDULED',
    NOW(), NOW();

-- Santos → Buenos Aires (Cap San Diego) — cabotagem Mercosul
INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT
    'b0000001-0000-0000-0000-000000000003',
    'CAP-2026-015',
    'a0000001-0000-0000-0000-000000000003',
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    NOW() - INTERVAL '2 days',
    NOW() + INTERVAL '3 days',
    'DEPARTED',
    NOW(), NOW();

-- Singapore → Santos (Maersk Edmonton)
INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT
    'b0000001-0000-0000-0000-000000000004',
    'MAE-2026-088',
    'a0000001-0000-0000-0000-000000000004',
    (SELECT id FROM ports WHERE unlocode = 'SGSIN'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    NOW() - INTERVAL '15 days',
    NOW() + INTERVAL '10 days',
    'IN_TRANSIT',
    NOW(), NOW();
