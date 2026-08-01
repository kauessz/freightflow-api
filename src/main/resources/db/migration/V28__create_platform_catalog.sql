CREATE TABLE platform_features (
    feature_key VARCHAR(80) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    value_type VARCHAR(30) NOT NULL,
    unit VARCHAR(50),
    implementation_status VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_platform_features_key_not_blank
        CHECK (BTRIM(feature_key) <> ''),
    CONSTRAINT chk_platform_features_key_canonical
        CHECK (feature_key = UPPER(BTRIM(feature_key)) AND feature_key ~ '^[A-Z][A-Z0-9_]*$'),
    CONSTRAINT chk_platform_features_name_not_blank
        CHECK (BTRIM(name) <> ''),
    CONSTRAINT chk_platform_features_value_type
        CHECK (value_type IN ('BOOLEAN', 'INTEGER_LIMIT')),
    CONSTRAINT chk_platform_features_unit_not_blank
        CHECK (unit IS NULL OR BTRIM(unit) <> ''),
    CONSTRAINT chk_platform_features_implementation_status
        CHECK (implementation_status IN ('AVAILABLE', 'PARTIAL', 'PLANNED'))
);

CREATE TABLE platform_feature_dependencies (
    feature_key VARCHAR(80) NOT NULL,
    required_feature_key VARCHAR(80) NOT NULL,
    CONSTRAINT pk_platform_feature_dependencies PRIMARY KEY (feature_key, required_feature_key),
    CONSTRAINT fk_platform_feature_dependencies_feature
        FOREIGN KEY (feature_key) REFERENCES platform_features(feature_key)
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    CONSTRAINT fk_platform_feature_dependencies_required_feature
        FOREIGN KEY (required_feature_key) REFERENCES platform_features(feature_key)
        ON DELETE RESTRICT
        ON UPDATE NO ACTION,
    CONSTRAINT chk_platform_feature_dependencies_not_self
        CHECK (feature_key <> required_feature_key)
);

CREATE TABLE subscription_plans (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    display_order INTEGER NOT NULL,
    custom BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_subscription_plans_code UNIQUE (code),
    CONSTRAINT chk_subscription_plans_code_not_blank
        CHECK (BTRIM(code) <> ''),
    CONSTRAINT chk_subscription_plans_code_canonical
        CHECK (code = UPPER(BTRIM(code)) AND code ~ '^[A-Z][A-Z0-9_]*$'),
    CONSTRAINT chk_subscription_plans_name_not_blank
        CHECK (BTRIM(name) <> ''),
    CONSTRAINT chk_subscription_plans_status
        CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED')),
    CONSTRAINT chk_subscription_plans_display_order_non_negative
        CHECK (display_order >= 0)
);

CREATE TABLE plan_entitlements (
    plan_id UUID NOT NULL,
    feature_key VARCHAR(80) NOT NULL,
    enabled BOOLEAN NOT NULL,
    limit_value INTEGER,
    CONSTRAINT pk_plan_entitlements PRIMARY KEY (plan_id, feature_key),
    CONSTRAINT fk_plan_entitlements_plan
        FOREIGN KEY (plan_id) REFERENCES subscription_plans(id)
        ON DELETE RESTRICT
        ON UPDATE NO ACTION,
    CONSTRAINT fk_plan_entitlements_feature
        FOREIGN KEY (feature_key) REFERENCES platform_features(feature_key)
        ON DELETE RESTRICT
        ON UPDATE NO ACTION,
    CONSTRAINT chk_plan_entitlements_disabled_without_limit
        CHECK (enabled OR limit_value IS NULL),
    CONSTRAINT chk_plan_entitlements_limit_value_non_negative
        CHECK (limit_value IS NULL OR limit_value >= 0)
);

INSERT INTO platform_features (
    feature_key, name, description, value_type, unit, implementation_status, active, created_at, updated_at
) VALUES
    ('SHIPMENT_MANAGEMENT', 'Shipment Management', 'Core shipment registration and operational CRUD.', 'BOOLEAN', NULL, 'AVAILABLE', TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('TRACKING', 'Tracking', 'Shipment tracking timeline and vessel-linked tracking flows.', 'BOOLEAN', NULL, 'AVAILABLE', TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('FLEET_MAP', 'Fleet Map', 'Operational fleet map with vessel positions and shipment context.', 'BOOLEAN', NULL, 'AVAILABLE', TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('AIS_TRACKING', 'AIS Tracking', 'Premium AIS-backed vessel position tracking and enrichment.', 'BOOLEAN', NULL, 'PARTIAL', TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('DOCUMENT_MANAGEMENT', 'Document Management', 'Tenant and customer document upload and retrieval.', 'BOOLEAN', NULL, 'AVAILABLE', TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('COMMERCIAL_RFQ', 'Commercial RFQ', 'Internal request-for-quotation workflow for logistics deals.', 'BOOLEAN', NULL, 'AVAILABLE', TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('QUOTATION_WORKFLOW', 'Quotation Workflow', 'Quotation drafting, review, approval and sending lifecycle.', 'BOOLEAN', NULL, 'AVAILABLE', TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('CLIENT_PORTAL', 'Client Portal', 'Customer-facing RFQ and quotation visibility endpoints.', 'BOOLEAN', NULL, 'PARTIAL', TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('BOOKING_MANAGEMENT', 'Booking Management', 'Future booking lifecycle built on commercial execution.', 'BOOLEAN', NULL, 'PLANNED', TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('COMMERCIAL_AGREEMENTS', 'Commercial Agreements', 'Future long-term commercial agreement workflows.', 'BOOLEAN', NULL, 'PLANNED', TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('REPORTS', 'Reports', 'Operational and commercial reporting surfaces.', 'BOOLEAN', NULL, 'PARTIAL', TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('API_ACCESS', 'API Access', 'Programmatic access and API-key based integrations.', 'BOOLEAN', NULL, 'PARTIAL', TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('WEBHOOKS', 'Webhooks', 'Outbound webhook subscriptions for integration events.', 'BOOLEAN', NULL, 'AVAILABLE', TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('MAX_ACTIVE_USERS', 'Max Active Users', 'Maximum number of active internal users allowed.', 'INTEGER_LIMIT', 'USERS', 'AVAILABLE', TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('MAX_CLIENT_USERS', 'Max Client Users', 'Maximum number of active client users allowed.', 'INTEGER_LIMIT', 'USERS', 'AVAILABLE', TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('MAX_MONTHLY_RFQS', 'Max Monthly RFQs', 'Maximum number of RFQs created in a monthly cycle.', 'INTEGER_LIMIT', 'RFQS_PER_MONTH', 'AVAILABLE', TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('MAX_MONTHLY_SHIPMENTS', 'Max Monthly Shipments', 'Maximum number of shipments created in a monthly cycle.', 'INTEGER_LIMIT', 'SHIPMENTS_PER_MONTH', 'AVAILABLE', TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('MAX_STORAGE_MB', 'Max Storage', 'Maximum document storage allocation.', 'INTEGER_LIMIT', 'MB', 'AVAILABLE', TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('MAX_MONTHLY_API_REQUESTS', 'Max Monthly API Requests', 'Maximum number of API requests per month.', 'INTEGER_LIMIT', 'REQUESTS_PER_MONTH', 'AVAILABLE', TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00');

INSERT INTO platform_feature_dependencies (feature_key, required_feature_key) VALUES
    ('FLEET_MAP', 'TRACKING'),
    ('AIS_TRACKING', 'TRACKING'),
    ('QUOTATION_WORKFLOW', 'COMMERCIAL_RFQ'),
    ('CLIENT_PORTAL', 'COMMERCIAL_RFQ'),
    ('COMMERCIAL_AGREEMENTS', 'QUOTATION_WORKFLOW');

INSERT INTO subscription_plans (
    id, code, name, description, status, display_order, custom, created_at, updated_at
) VALUES
    ('0fbe6f06-6416-4c8a-9382-6a5ff1a4a101', 'STARTER', 'Starter', 'Entry-level operational package for small freight operators.', 'ACTIVE', 0, FALSE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('3f7a13e2-1cf4-45c0-a5f2-3ff7d9bdb102', 'PROFESSIONAL', 'Professional', 'Expanded operational and commercial package for growing operators.', 'ACTIVE', 1, FALSE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('b7c31d61-7afb-4718-b9c9-b9c59fc0c103', 'ENTERPRISE', 'Enterprise', 'High-capacity package with premium integrations and planned modules.', 'ACTIVE', 2, FALSE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00'),
    ('d8b2a1f5-41d2-4f76-9d4b-89db7351d104', 'CUSTOM', 'Custom', 'Template plan for bespoke contracts configured in future phases.', 'DRAFT', 3, TRUE, TIMESTAMP '2026-08-01 00:00:00', TIMESTAMP '2026-08-01 00:00:00');

INSERT INTO plan_entitlements (plan_id, feature_key, enabled, limit_value) VALUES
    ('0fbe6f06-6416-4c8a-9382-6a5ff1a4a101', 'SHIPMENT_MANAGEMENT', TRUE, NULL),
    ('0fbe6f06-6416-4c8a-9382-6a5ff1a4a101', 'TRACKING', TRUE, NULL),
    ('0fbe6f06-6416-4c8a-9382-6a5ff1a4a101', 'DOCUMENT_MANAGEMENT', TRUE, NULL),
    ('0fbe6f06-6416-4c8a-9382-6a5ff1a4a101', 'MAX_ACTIVE_USERS', TRUE, 5),
    ('0fbe6f06-6416-4c8a-9382-6a5ff1a4a101', 'MAX_MONTHLY_SHIPMENTS', TRUE, 100),
    ('0fbe6f06-6416-4c8a-9382-6a5ff1a4a101', 'MAX_STORAGE_MB', TRUE, 1024),

    ('3f7a13e2-1cf4-45c0-a5f2-3ff7d9bdb102', 'SHIPMENT_MANAGEMENT', TRUE, NULL),
    ('3f7a13e2-1cf4-45c0-a5f2-3ff7d9bdb102', 'TRACKING', TRUE, NULL),
    ('3f7a13e2-1cf4-45c0-a5f2-3ff7d9bdb102', 'DOCUMENT_MANAGEMENT', TRUE, NULL),
    ('3f7a13e2-1cf4-45c0-a5f2-3ff7d9bdb102', 'FLEET_MAP', TRUE, NULL),
    ('3f7a13e2-1cf4-45c0-a5f2-3ff7d9bdb102', 'COMMERCIAL_RFQ', TRUE, NULL),
    ('3f7a13e2-1cf4-45c0-a5f2-3ff7d9bdb102', 'QUOTATION_WORKFLOW', TRUE, NULL),
    ('3f7a13e2-1cf4-45c0-a5f2-3ff7d9bdb102', 'CLIENT_PORTAL', TRUE, NULL),
    ('3f7a13e2-1cf4-45c0-a5f2-3ff7d9bdb102', 'REPORTS', TRUE, NULL),
    ('3f7a13e2-1cf4-45c0-a5f2-3ff7d9bdb102', 'MAX_ACTIVE_USERS', TRUE, 25),
    ('3f7a13e2-1cf4-45c0-a5f2-3ff7d9bdb102', 'MAX_CLIENT_USERS', TRUE, 10),
    ('3f7a13e2-1cf4-45c0-a5f2-3ff7d9bdb102', 'MAX_MONTHLY_RFQS', TRUE, 200),
    ('3f7a13e2-1cf4-45c0-a5f2-3ff7d9bdb102', 'MAX_MONTHLY_SHIPMENTS', TRUE, 1000),
    ('3f7a13e2-1cf4-45c0-a5f2-3ff7d9bdb102', 'MAX_STORAGE_MB', TRUE, 10240),

    ('b7c31d61-7afb-4718-b9c9-b9c59fc0c103', 'SHIPMENT_MANAGEMENT', TRUE, NULL),
    ('b7c31d61-7afb-4718-b9c9-b9c59fc0c103', 'TRACKING', TRUE, NULL),
    ('b7c31d61-7afb-4718-b9c9-b9c59fc0c103', 'FLEET_MAP', TRUE, NULL),
    ('b7c31d61-7afb-4718-b9c9-b9c59fc0c103', 'AIS_TRACKING', TRUE, NULL),
    ('b7c31d61-7afb-4718-b9c9-b9c59fc0c103', 'DOCUMENT_MANAGEMENT', TRUE, NULL),
    ('b7c31d61-7afb-4718-b9c9-b9c59fc0c103', 'COMMERCIAL_RFQ', TRUE, NULL),
    ('b7c31d61-7afb-4718-b9c9-b9c59fc0c103', 'QUOTATION_WORKFLOW', TRUE, NULL),
    ('b7c31d61-7afb-4718-b9c9-b9c59fc0c103', 'CLIENT_PORTAL', TRUE, NULL),
    ('b7c31d61-7afb-4718-b9c9-b9c59fc0c103', 'REPORTS', TRUE, NULL),
    ('b7c31d61-7afb-4718-b9c9-b9c59fc0c103', 'API_ACCESS', TRUE, NULL),
    ('b7c31d61-7afb-4718-b9c9-b9c59fc0c103', 'WEBHOOKS', TRUE, NULL),
    ('b7c31d61-7afb-4718-b9c9-b9c59fc0c103', 'MAX_ACTIVE_USERS', TRUE, NULL),
    ('b7c31d61-7afb-4718-b9c9-b9c59fc0c103', 'MAX_CLIENT_USERS', TRUE, NULL),
    ('b7c31d61-7afb-4718-b9c9-b9c59fc0c103', 'MAX_MONTHLY_RFQS', TRUE, NULL),
    ('b7c31d61-7afb-4718-b9c9-b9c59fc0c103', 'MAX_MONTHLY_SHIPMENTS', TRUE, NULL),
    ('b7c31d61-7afb-4718-b9c9-b9c59fc0c103', 'MAX_STORAGE_MB', TRUE, NULL),
    ('b7c31d61-7afb-4718-b9c9-b9c59fc0c103', 'MAX_MONTHLY_API_REQUESTS', TRUE, NULL);
