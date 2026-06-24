-- ============================================================
-- V23 — Documents table
-- Stores file metadata for documents attached to shipments
-- or voyages. Actual file content lives in Cloudflare R2.
-- ============================================================

CREATE TABLE documents (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    shipment_id     UUID REFERENCES shipments(id) ON DELETE CASCADE,
    voyage_id       UUID REFERENCES voyages(id)   ON DELETE SET NULL,
    type            VARCHAR(30)  NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    storage_key     VARCHAR(500) NOT NULL,
    content_type    VARCHAR(100),
    size_bytes      BIGINT,
    uploaded_by     UUID REFERENCES users(id),
    uploaded_at     TIMESTAMP WITH TIME ZONE DEFAULT now(),
    description     VARCHAR(500),
    active          BOOLEAN NOT NULL DEFAULT true
);

-- Fast lookup of active documents for a given shipment
CREATE INDEX idx_documents_shipment
    ON documents(shipment_id)
    WHERE active = true;

-- Tenant-scoped listing ordered by upload time (newest first)
CREATE INDEX idx_documents_tenant
    ON documents(tenant_id, uploaded_at DESC);
