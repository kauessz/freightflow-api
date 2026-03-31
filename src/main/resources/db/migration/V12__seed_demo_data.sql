-- ============================================================
-- V12: Seed demo data - cabotagem e longo curso brasileiro
-- Dados realistas de mercado: Mercosul Line, CMA CGM, Maersk,
-- MSC, Hapag-Lloyd
-- ============================================================

-- ==================== Tenant de demo ====================
INSERT INTO tenants (id, name, slug, contact_email, active, plan, created_at, updated_at)
VALUES (
    'f0000001-0000-0000-0000-000000000001',
    'Freightflow Demo',
    'freightflow-demo',
    'demo@freightflow.io',
    true,
    'PROFESSIONAL',
    NOW(), NOW()
) ON CONFLICT (slug) DO NOTHING;

-- ==================== Usuario demo ====================
-- Senha: Demo@2026 (BCrypt hash)
INSERT INTO users (id, name, email, password_hash, role, tenant_id, active, created_at, updated_at)
SELECT
    'e0000001-0000-0000-0000-000000000001',
    'Kaue Silva',
    'demo@freightflow.io',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    id,
    true,
    NOW(), NOW()
FROM tenants WHERE slug = 'freightflow-demo'
ON CONFLICT (email) DO NOTHING;

-- ==================== Vessels (frota real) ====================
INSERT INTO vessels (id, imo, name, flag, type, capacity_teu, created_at, updated_at) VALUES
('a0000002-0000-0000-0000-000000000001', '9321483', 'CAP SAN MARCO',   'DE', 'CONTAINER', 9814, NOW(), NOW()),
('a0000002-0000-0000-0000-000000000002', '9354923', 'CAP SAN NICOLAS', 'DE', 'CONTAINER', 9814, NOW(), NOW()),
('a0000002-0000-0000-0000-000000000003', '9492713', 'MONTE TAMARO',    'DE', 'CONTAINER', 2700, NOW(), NOW())
ON CONFLICT (imo) DO NOTHING;

-- ==================== Voyages (rotas PLATA e LATA) ====================

-- 0PLATS1MA: Santos -> Buenos Aires (CAP SAN MARCO) - IN_TRANSIT
INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, atd, status, created_at, updated_at)
SELECT
    'b0000002-0000-0000-0000-000000000001',
    '0PLATS1MA',
    'a0000002-0000-0000-0000-000000000001',
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    NOW() - INTERVAL '3 days',
    NOW() + INTERVAL '2 days',
    NOW() - INTERVAL '3 days',
    'IN_TRANSIT',
    NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '0PLATS1MA');

-- 0PLATS2MA: Buenos Aires -> Santos (CAP SAN NICOLAS) - SCHEDULED
INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, status, created_at, updated_at)
SELECT
    'b0000002-0000-0000-0000-000000000002',
    '0PLATS2MA',
    'a0000002-0000-0000-0000-000000000002',
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    NOW() + INTERVAL '5 days',
    NOW() + INTERVAL '10 days',
    'SCHEDULED',
    NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '0PLATS2MA');

-- 0LATAS1MA: Santos -> Rotterdam (MONTE TAMARO) - IN_TRANSIT
INSERT INTO voyages (id, voyage_number, vessel_id, origin_port_id, destination_port_id, etd, eta, atd, status, created_at, updated_at)
SELECT
    'b0000002-0000-0000-0000-000000000003',
    '0LATAS1MA',
    'a0000002-0000-0000-0000-000000000003',
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM ports WHERE unlocode = 'NLRTM'),
    NOW() - INTERVAL '10 days',
    NOW() + INTERVAL '12 days',
    NOW() - INTERVAL '10 days',
    'IN_TRANSIT',
    NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM voyages WHERE voyage_number = '0LATAS1MA');

-- ==================== Shipments (10 embarques variados) ====================

-- 1. Mercosul Line - IN_TRANSIT (Santos -> Buenos Aires)
INSERT INTO shipments (id, booking, container_number, container_type, voyage_id, origin_port_id, destination_port_id, tenant_id, consignee, shipper, status, created_at, updated_at)
SELECT
    'c0000001-0000-0000-0000-000000000001', 'P10482561', 'MSCU7234561', 'TEU40',
    'b0000002-0000-0000-0000-000000000001',
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'Cargill Argentina S.A.', 'Bunge Alimentos S.A.',
    'IN_TRANSIT', NOW() - INTERVAL '5 days', NOW()
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'P10482561');

-- 2. Maersk - LOADED (Santos -> Buenos Aires)
INSERT INTO shipments (id, booking, container_number, container_type, voyage_id, origin_port_id, destination_port_id, tenant_id, consignee, shipper, status, created_at, updated_at)
SELECT
    'c0000001-0000-0000-0000-000000000002', '570000000', 'MSKU9887654', 'TEU20',
    'b0000002-0000-0000-0000-000000000001',
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'Molinos Rio de la Plata S.A.', 'JBS S.A.',
    'LOADED', NOW() - INTERVAL '4 days', NOW()
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = '570000000');

-- 3. MSC - IN_TRANSIT (Santos -> Rotterdam)
INSERT INTO shipments (id, booking, container_number, container_type, voyage_id, origin_port_id, destination_port_id, tenant_id, consignee, shipper, status, created_at, updated_at)
SELECT
    'c0000001-0000-0000-0000-000000000003', 'MEDU1234567', 'MEDU4455667', 'TEU40HC',
    'b0000002-0000-0000-0000-000000000003',
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM ports WHERE unlocode = 'NLRTM'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'Unilever Europe B.V.', 'Suzano Papel e Celulose S.A.',
    'IN_TRANSIT', NOW() - INTERVAL '12 days', NOW()
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MEDU1234567');

-- 4. CMA CGM - BOOKED (Buenos Aires -> Santos)
INSERT INTO shipments (id, booking, container_number, container_type, voyage_id, origin_port_id, destination_port_id, tenant_id, consignee, shipper, status, created_at, updated_at)
SELECT
    'c0000001-0000-0000-0000-000000000004', 'GRI0123456', NULL, 'REEFER40',
    'b0000002-0000-0000-0000-000000000002',
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'Frigorifico Minerva S.A.', 'Frigorifico Rioplatense S.A.',
    'BOOKED', NOW() - INTERVAL '1 day', NOW()
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'GRI0123456');

-- 5. Hapag-Lloyd - CONFIRMED (Buenos Aires -> Santos)
INSERT INTO shipments (id, booking, container_number, container_type, voyage_id, origin_port_id, destination_port_id, tenant_id, consignee, shipper, status, created_at, updated_at)
SELECT
    'c0000001-0000-0000-0000-000000000005', '1234567890', 'HLXU8876543', 'TEU40',
    'b0000002-0000-0000-0000-000000000002',
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'Ambev S.A.', 'Arcor S.A.I.C.',
    'CONFIRMED', NOW() - INTERVAL '2 days', NOW()
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = '1234567890');

-- 6. Mercosul Line - GATE_IN (Santos -> Buenos Aires)
INSERT INTO shipments (id, booking, container_number, container_type, voyage_id, origin_port_id, destination_port_id, tenant_id, consignee, shipper, status, created_at, updated_at)
SELECT
    'c0000001-0000-0000-0000-000000000006', 'P10498732', 'TCLU3344556', 'TEU20',
    'b0000002-0000-0000-0000-000000000001',
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'YPF S.A.', 'Petrobras S.A.',
    'GATE_IN', NOW() - INTERVAL '3 days', NOW()
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'P10498732');

-- 7. MSC - ARRIVED (Santos -> Rotterdam)
INSERT INTO shipments (id, booking, container_number, container_type, voyage_id, origin_port_id, destination_port_id, tenant_id, consignee, shipper, status, created_at, updated_at)
SELECT
    'c0000001-0000-0000-0000-000000000007', 'MEDU7654321', 'MEDU1122334', 'REEFER40',
    'b0000002-0000-0000-0000-000000000003',
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM ports WHERE unlocode = 'NLRTM'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'Albert Heijn B.V.', 'Citrosuco S.A.',
    'ARRIVED', NOW() - INTERVAL '12 days', NOW()
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'MEDU7654321');

-- 8. CMA CGM - IN_TRANSIT (Santos -> Rotterdam)
INSERT INTO shipments (id, booking, container_number, container_type, voyage_id, origin_port_id, destination_port_id, tenant_id, consignee, shipper, status, created_at, updated_at)
SELECT
    'c0000001-0000-0000-0000-000000000008', 'GRI0987654', 'CMAU5566778', 'TEU40HC',
    'b0000002-0000-0000-0000-000000000003',
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM ports WHERE unlocode = 'NLRTM'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'BASF SE', 'Braskem S.A.',
    'IN_TRANSIT', NOW() - INTERVAL '11 days', NOW()
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = 'GRI0987654');

-- 9. Maersk - DELIVERED (Santos -> Buenos Aires)
INSERT INTO shipments (id, booking, container_number, container_type, voyage_id, origin_port_id, destination_port_id, tenant_id, consignee, shipper, status, created_at, updated_at)
SELECT
    'c0000001-0000-0000-0000-000000000009', '570111222', 'MSKU1234000', 'TEU40',
    'b0000002-0000-0000-0000-000000000001',
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'Techint S.A.', 'Gerdau S.A.',
    'DELIVERED', NOW() - INTERVAL '20 days', NOW()
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = '570111222');

-- 10. Hapag-Lloyd - CANCELLED
INSERT INTO shipments (id, booking, container_number, container_type, voyage_id, origin_port_id, destination_port_id, tenant_id, consignee, shipper, status, created_at, updated_at)
SELECT
    'c0000001-0000-0000-0000-000000000010', '1234000999', NULL, NULL,
    'b0000002-0000-0000-0000-000000000002',
    (SELECT id FROM ports WHERE unlocode = 'ARBUE'),
    (SELECT id FROM ports WHERE unlocode = 'BRSSZ'),
    (SELECT id FROM tenants WHERE slug = 'freightflow-demo'),
    'Natura Co S.A.', 'Grupo Perez Companc',
    'CANCELLED', NOW() - INTERVAL '7 days', NOW()
WHERE NOT EXISTS (SELECT 1 FROM shipments WHERE booking = '1234000999');

-- ==================== Events ====================

-- Embarque 1: P10482561 (Mercosul, IN_TRANSIT Santos->BsAs)
INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(), 'c0000001-0000-0000-0000-000000000001', 'GATE_IN',
    'Santos, BR', 'Container MSCU7234561 arrived at Santos terminal gate',
    NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE shipment_id = 'c0000001-0000-0000-0000-000000000001' AND type = 'GATE_IN');

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(), 'c0000001-0000-0000-0000-000000000001', 'LOADED',
    'Santos, BR', 'Container loaded onto CAP SAN MARCO at berth 37',
    NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE shipment_id = 'c0000001-0000-0000-0000-000000000001' AND type = 'LOADED');

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(), 'c0000001-0000-0000-0000-000000000001', 'DEPARTED',
    'Santos, BR', 'Vessel CAP SAN MARCO departed Santos bound for Buenos Aires',
    NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE shipment_id = 'c0000001-0000-0000-0000-000000000001' AND type = 'DEPARTED');

-- Embarque 3: MEDU1234567 (MSC, IN_TRANSIT Santos->Rotterdam)
INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(), 'c0000001-0000-0000-0000-000000000003', 'GATE_IN',
    'Santos, BR', 'Container MEDU4455667 cleared for entry at Santos',
    NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE shipment_id = 'c0000001-0000-0000-0000-000000000003' AND type = 'GATE_IN');

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(), 'c0000001-0000-0000-0000-000000000003', 'LOADED',
    'Santos, BR', 'Container loaded onto MONTE TAMARO at berth 12',
    NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE shipment_id = 'c0000001-0000-0000-0000-000000000003' AND type = 'LOADED');

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(), 'c0000001-0000-0000-0000-000000000003', 'DEPARTED',
    'Santos, BR', 'Vessel MONTE TAMARO departed Santos bound for Rotterdam',
    NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE shipment_id = 'c0000001-0000-0000-0000-000000000003' AND type = 'DEPARTED');

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(), 'c0000001-0000-0000-0000-000000000003', 'TRANSSHIPMENT',
    'Tanger Med, MA', 'Cargo transshipped at Tanger Med hub',
    NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE shipment_id = 'c0000001-0000-0000-0000-000000000003' AND type = 'TRANSSHIPMENT');

-- Embarque 7: MEDU7654321 (MSC, ARRIVED Santos->Rotterdam)
INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(), 'c0000001-0000-0000-0000-000000000007', 'GATE_IN',
    'Santos, BR', 'Reefer container MEDU1122334 connected at Santos cold storage',
    NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE shipment_id = 'c0000001-0000-0000-0000-000000000007' AND type = 'GATE_IN');

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(), 'c0000001-0000-0000-0000-000000000007', 'LOADED',
    'Santos, BR', 'Reefer loaded onto MONTE TAMARO at berth 12',
    NOW() - INTERVAL '24 days', NOW() - INTERVAL '24 days'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE shipment_id = 'c0000001-0000-0000-0000-000000000007' AND type = 'LOADED');

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(), 'c0000001-0000-0000-0000-000000000007', 'DEPARTED',
    'Santos, BR', 'Vessel MONTE TAMARO departed Santos',
    NOW() - INTERVAL '22 days', NOW() - INTERVAL '22 days'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE shipment_id = 'c0000001-0000-0000-0000-000000000007' AND type = 'DEPARTED');

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(), 'c0000001-0000-0000-0000-000000000007', 'ARRIVED',
    'Rotterdam, NL', 'Vessel arrived at Europoort terminal, Rotterdam',
    NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE shipment_id = 'c0000001-0000-0000-0000-000000000007' AND type = 'ARRIVED');

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(), 'c0000001-0000-0000-0000-000000000007', 'CUSTOMS_RELEASE',
    'Rotterdam, NL', 'Customs clearance completed - released for delivery',
    NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE shipment_id = 'c0000001-0000-0000-0000-000000000007' AND type = 'CUSTOMS_RELEASE');

-- Embarque 8: GRI0987654 (CMA CGM, IN_TRANSIT Santos->Rotterdam)
INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(), 'c0000001-0000-0000-0000-000000000008', 'GATE_IN',
    'Santos, BR', 'Container CMAU5566778 entered Santos terminal',
    NOW() - INTERVAL '13 days', NOW() - INTERVAL '13 days'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE shipment_id = 'c0000001-0000-0000-0000-000000000008' AND type = 'GATE_IN');

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(), 'c0000001-0000-0000-0000-000000000008', 'LOADED',
    'Santos, BR', 'Container loaded onto MONTE TAMARO',
    NOW() - INTERVAL '11 days', NOW() - INTERVAL '11 days'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE shipment_id = 'c0000001-0000-0000-0000-000000000008' AND type = 'LOADED');

INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(), 'c0000001-0000-0000-0000-000000000008', 'DEPARTED',
    'Santos, BR', 'Vessel MONTE TAMARO departed Santos bound for Rotterdam',
    NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE shipment_id = 'c0000001-0000-0000-0000-000000000008' AND type = 'DEPARTED');

-- Embarque 6: P10498732 (Mercosul, GATE_IN)
INSERT INTO events (id, shipment_id, type, location, description, occurred_at, reported_at)
SELECT gen_random_uuid(), 'c0000001-0000-0000-0000-000000000006', 'GATE_IN',
    'Santos, BR', 'Container TCLU3344556 arrived at Santos gate 4',
    NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'
WHERE NOT EXISTS (SELECT 1 FROM events WHERE shipment_id = 'c0000001-0000-0000-0000-000000000006' AND type = 'GATE_IN');