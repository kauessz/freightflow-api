-- ============================================================
-- V16: RBAC — customers table, new roles, seed data
-- Roles: ADMIN, OPERATOR, VIEWER, CLIENT
-- ============================================================

-- ==================== Customers table ====================
CREATE TABLE IF NOT EXISTS customers (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID NOT NULL REFERENCES tenants(id),
    name        VARCHAR(255) NOT NULL,
    tax_id      VARCHAR(50),
    contact_name  VARCHAR(255),
    contact_email VARCHAR(255),
    active      BOOLEAN NOT NULL DEFAULT true,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_customers_tenant ON customers(tenant_id);
CREATE INDEX IF NOT EXISTS idx_customers_active ON customers(active);

-- ==================== Add customer_id to users ====================
ALTER TABLE users ADD COLUMN IF NOT EXISTS customer_id UUID REFERENCES customers(id);

-- ==================== Add customer_id to shipments ====================
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS customer_id UUID REFERENCES customers(id);

CREATE INDEX IF NOT EXISTS idx_shipments_customer ON shipments(customer_id);

-- ==================== Update role CHECK constraint ====================
-- Drop old constraint (role IN ('ADMIN', 'USER', 'VIEWER'))
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

-- Add new constraint with OPERATOR + CLIENT
ALTER TABLE users ADD CONSTRAINT users_role_check
    CHECK (role IN ('ADMIN', 'OPERATOR', 'VIEWER', 'CLIENT'));

-- ==================== Seed 5 customers (demo tenant) ====================
INSERT INTO customers (id, tenant_id, name, tax_id, contact_name, contact_email, active, created_at, updated_at)
SELECT
    'c0000001-0000-0000-0000-000000000001',
    t.id,
    'Atlas Cargo Ltda',
    '12.345.678/0001-90',
    'Fernando Ramos',
    'fernando.ramos@atlascargo.com.br',
    true, NOW(), NOW()
FROM tenants t WHERE t.slug = 'freightflow-demo'
ON CONFLICT DO NOTHING;

INSERT INTO customers (id, tenant_id, name, tax_id, contact_name, contact_email, active, created_at, updated_at)
SELECT
    'c0000002-0000-0000-0000-000000000002',
    t.id,
    'Meridian Imports S.A.',
    '98.765.432/0001-10',
    'Carla Mendes',
    'carla@meridiansg.com',
    true, NOW(), NOW()
FROM tenants t WHERE t.slug = 'freightflow-demo'
ON CONFLICT DO NOTHING;

INSERT INTO customers (id, tenant_id, name, tax_id, contact_name, contact_email, active, created_at, updated_at)
SELECT
    'c0000003-0000-0000-0000-000000000003',
    t.id,
    'Rota Livre Transportes',
    '55.111.222/0001-33',
    'Roberto Alves',
    'r.alves@rotalivre.com.br',
    true, NOW(), NOW()
FROM tenants t WHERE t.slug = 'freightflow-demo'
ON CONFLICT DO NOTHING;

INSERT INTO customers (id, tenant_id, name, tax_id, contact_name, contact_email, active, created_at, updated_at)
SELECT
    'c0000004-0000-0000-0000-000000000004',
    t.id,
    'Global Trade Partners',
    '77.444.555/0001-66',
    'Ana Souza',
    'ana.souza@globaltp.io',
    true, NOW(), NOW()
FROM tenants t WHERE t.slug = 'freightflow-demo'
ON CONFLICT DO NOTHING;

INSERT INTO customers (id, tenant_id, name, tax_id, contact_name, contact_email, active, created_at, updated_at)
SELECT
    'c0000005-0000-0000-0000-000000000005',
    t.id,
    'Oceanic Freight Solutions',
    '33.999.888/0001-55',
    'Marcos Lima',
    'marcos@oceanicfreight.com',
    true, NOW(), NOW()
FROM tenants t WHERE t.slug = 'freightflow-demo'
ON CONFLICT DO NOTHING;

-- ==================== Seed demo users ====================
-- Senha para todos: Demo@2026
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

-- OPERATOR
INSERT INTO users (id, name, email, password_hash, role, tenant_id, active, created_at, updated_at)
SELECT
    'e0000002-0000-0000-0000-000000000002',
    'Operador Demo',
    'operador@freightflow.io',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'OPERATOR',
    t.id,
    true, NOW(), NOW()
FROM tenants t WHERE t.slug = 'freightflow-demo'
ON CONFLICT (email) DO NOTHING;

-- VIEWER
INSERT INTO users (id, name, email, password_hash, role, tenant_id, active, created_at, updated_at)
SELECT
    'e0000003-0000-0000-0000-000000000003',
    'Viewer Demo',
    'viewer@freightflow.io',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'VIEWER',
    t.id,
    true, NOW(), NOW()
FROM tenants t WHERE t.slug = 'freightflow-demo'
ON CONFLICT (email) DO NOTHING;

-- CLIENT (linked to Atlas Cargo customer)
INSERT INTO users (id, name, email, password_hash, role, tenant_id, customer_id, active, created_at, updated_at)
SELECT
    'e0000004-0000-0000-0000-000000000004',
    'Cliente Atlas Cargo',
    'cliente@atlascargo.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'CLIENT',
    t.id,
    'c0000001-0000-0000-0000-000000000001',
    true, NOW(), NOW()
FROM tenants t WHERE t.slug = 'freightflow-demo'
ON CONFLICT (email) DO NOTHING;

-- ==================== Assign some shipments to Atlas Cargo ====================
-- Assign first 15 shipments of the demo tenant to Atlas Cargo customer
UPDATE shipments
SET customer_id = 'c0000001-0000-0000-0000-000000000001'
WHERE id IN (
    SELECT s.id FROM shipments s
    JOIN tenants t ON s.tenant_id = t.id
    WHERE t.slug = 'freightflow-demo'
    ORDER BY s.created_at
    LIMIT 15
)
AND customer_id IS NULL;

-- Assign next 10 to Meridian Imports
UPDATE shipments
SET customer_id = 'c0000002-0000-0000-0000-000000000002'
WHERE id IN (
    SELECT s.id FROM shipments s
    JOIN tenants t ON s.tenant_id = t.id
    WHERE t.slug = 'freightflow-demo'
    AND s.customer_id IS NULL
    ORDER BY s.created_at
    LIMIT 10
)
AND customer_id IS NULL;
