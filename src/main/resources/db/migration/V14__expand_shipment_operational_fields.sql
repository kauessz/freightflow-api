-- ============================================================
-- V14: Expand shipments table with full operational fields
--      for maritime agency management
-- ============================================================

-- Documentos BL
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS house_bl             VARCHAR(50);
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS master_bl            VARCHAR(50);
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS customer_reference   VARCHAR(50);

-- Partes do embarque
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS notify_party         VARCHAR(255);
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS operator_name        VARCHAR(100);

-- Dados operacionais
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS incoterm             VARCHAR(10);
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS freight_term         VARCHAR(20);
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS cargo_description    VARCHAR(255);
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS service_lane         VARCHAR(100);

-- Container detalhado
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS container_size_ft    INTEGER;
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS container_iso_code   VARCHAR(10);
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS gross_weight_kg      NUMERIC(12,2);
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS net_weight_kg        NUMERIC(12,2);
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS volume_cbm           NUMERIC(10,2);
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS packages             INTEGER;
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS package_type         VARCHAR(50);

-- Transbordo
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS transshipment_port_id UUID REFERENCES ports(id);

-- Status operacionais
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS document_status      VARCHAR(30) DEFAULT 'PENDING';
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS customs_status       VARCHAR(30) DEFAULT 'NOT_STARTED';
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS risk_level           VARCHAR(20) DEFAULT 'LOW';
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS delay_days           INTEGER DEFAULT 0;

-- Referência AIS
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS vessel_source_url    VARCHAR(500);
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS notes                TEXT;
