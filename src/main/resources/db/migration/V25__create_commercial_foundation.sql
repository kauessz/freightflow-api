CREATE TABLE commercial_rfqs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    reference VARCHAR(80) NOT NULL,
    customer_id UUID NULL REFERENCES customers(id),
    prospect_company_name VARCHAR(255),
    contact_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    direction VARCHAR(20) NOT NULL,
    transport_mode VARCHAR(20) NOT NULL,
    service_type VARCHAR(20) NOT NULL,
    incoterm_code VARCHAR(10),
    incoterm_version VARCHAR(10),
    incoterm_named_place VARCHAR(255),
    origin_port_id UUID NOT NULL REFERENCES ports(id),
    destination_port_id UUID NOT NULL REFERENCES ports(id),
    place_of_receipt VARCHAR(255),
    place_of_delivery VARCHAR(255),
    cargo_ready_date TIMESTAMP,
    desired_departure_date TIMESTAMP,
    status VARCHAR(30) NOT NULL,
    assigned_to UUID NULL REFERENCES users(id),
    notes TEXT,
    created_by UUID NOT NULL REFERENCES users(id),
    submitted_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_commercial_rfqs_tenant_reference UNIQUE (tenant_id, reference),
    CONSTRAINT uq_commercial_rfqs_id_tenant UNIQUE (id, tenant_id),
    CONSTRAINT chk_commercial_rfqs_direction CHECK (direction IN ('IMPORT', 'EXPORT')),
    CONSTRAINT chk_commercial_rfqs_transport_mode CHECK (transport_mode IN ('OCEAN', 'AIR', 'ROAD', 'RAIL', 'MULTIMODAL')),
    CONSTRAINT chk_commercial_rfqs_service_type CHECK (service_type IN ('FCL', 'LCL', 'BREAK_BULK')),
    CONSTRAINT chk_commercial_rfqs_status CHECK (status IN ('DRAFT', 'SUBMITTED', 'UNDER_ANALYSIS', 'QUOTED', 'CANCELLED', 'EXPIRED')),
    CONSTRAINT chk_commercial_rfqs_contact CHECK (contact_email IS NOT NULL OR contact_phone IS NOT NULL),
    CONSTRAINT chk_commercial_rfqs_customer_or_prospect CHECK (customer_id IS NOT NULL OR prospect_company_name IS NOT NULL),
    CONSTRAINT chk_commercial_rfqs_ports CHECK (origin_port_id <> destination_port_id),
    CONSTRAINT chk_commercial_rfqs_dates CHECK (
        cargo_ready_date IS NULL OR desired_departure_date IS NULL OR cargo_ready_date <= desired_departure_date
    ),
    CONSTRAINT chk_commercial_rfqs_incoterm_code CHECK (
        incoterm_code IS NULL OR incoterm_code IN ('EXW', 'FCA', 'CPT', 'CIP', 'DAP', 'DPU', 'DDP', 'FAS', 'FOB', 'CFR', 'CIF')
    ),
    CONSTRAINT chk_commercial_rfqs_incoterm_version CHECK (
        incoterm_version IS NULL OR incoterm_version = '2020'
    ),
    CONSTRAINT chk_commercial_rfqs_incoterm_ocean CHECK (
        incoterm_code IS NULL OR incoterm_code NOT IN ('FAS', 'FOB', 'CFR', 'CIF') OR transport_mode = 'OCEAN'
    ),
    CONSTRAINT chk_commercial_rfqs_incoterm CHECK (
        (incoterm_code IS NULL AND incoterm_version IS NULL AND incoterm_named_place IS NULL)
        OR (incoterm_code IS NOT NULL AND incoterm_version IS NOT NULL AND incoterm_named_place IS NOT NULL)
    )
);

CREATE INDEX idx_commercial_rfqs_tenant_status ON commercial_rfqs(tenant_id, status);
CREATE INDEX idx_commercial_rfqs_tenant_customer ON commercial_rfqs(tenant_id, customer_id);
CREATE INDEX idx_commercial_rfqs_tenant_assigned_to ON commercial_rfqs(tenant_id, assigned_to);
CREATE INDEX idx_commercial_rfqs_tenant_created_at ON commercial_rfqs(tenant_id, created_at);

CREATE TABLE commercial_rfq_cargo_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rfq_id UUID NOT NULL REFERENCES commercial_rfqs(id) ON DELETE CASCADE,
    description VARCHAR(500) NOT NULL,
    package_type VARCHAR(100),
    package_quantity INTEGER NOT NULL,
    gross_weight NUMERIC(14,3) NOT NULL,
    weight_unit VARCHAR(10) NOT NULL,
    volume NUMERIC(14,3),
    volume_unit VARCHAR(10),
    hs_code VARCHAR(50),
    dangerous_goods BOOLEAN NOT NULL DEFAULT FALSE,
    un_number VARCHAR(20),
    temperature_controlled BOOLEAN NOT NULL DEFAULT FALSE,
    minimum_temperature NUMERIC(8,2),
    maximum_temperature NUMERIC(8,2),
    stackable BOOLEAN,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_commercial_rfq_cargo_package_quantity CHECK (package_quantity > 0),
    CONSTRAINT chk_commercial_rfq_cargo_gross_weight CHECK (gross_weight > 0),
    CONSTRAINT chk_commercial_rfq_cargo_volume CHECK (volume IS NULL OR volume > 0),
    CONSTRAINT chk_commercial_rfq_cargo_weight_unit CHECK (weight_unit IN ('KG', 'LB')),
    CONSTRAINT chk_commercial_rfq_cargo_volume_unit CHECK (volume_unit IS NULL OR volume_unit IN ('CBM', 'CFT')),
    CONSTRAINT chk_commercial_rfq_cargo_dg CHECK (dangerous_goods = FALSE OR un_number IS NOT NULL),
    CONSTRAINT chk_commercial_rfq_cargo_temp CHECK (
        (temperature_controlled = FALSE AND minimum_temperature IS NULL AND maximum_temperature IS NULL)
        OR (
            temperature_controlled = TRUE
            AND minimum_temperature IS NOT NULL
            AND maximum_temperature IS NOT NULL
            AND minimum_temperature <= maximum_temperature
        )
    )
);

CREATE INDEX idx_commercial_rfq_cargo_rfq ON commercial_rfq_cargo_items(rfq_id);

CREATE TABLE commercial_rfq_containers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rfq_id UUID NOT NULL REFERENCES commercial_rfqs(id) ON DELETE CASCADE,
    container_type VARCHAR(30) NOT NULL,
    quantity INTEGER NOT NULL,
    weight_per_container NUMERIC(14,3),
    weight_unit VARCHAR(10),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_commercial_rfq_containers_type CHECK (
        container_type IN (
            'DRY_20', 'DRY_40', 'HIGH_CUBE_40', 'REEFER_20', 'REEFER_40',
            'OPEN_TOP_20', 'OPEN_TOP_40', 'FLAT_RACK_20', 'FLAT_RACK_40'
        )
    ),
    CONSTRAINT chk_commercial_rfq_containers_quantity CHECK (quantity > 0),
    CONSTRAINT chk_commercial_rfq_containers_weight CHECK (weight_per_container IS NULL OR weight_per_container > 0),
    CONSTRAINT chk_commercial_rfq_containers_weight_unit CHECK (weight_unit IS NULL OR weight_unit IN ('KG', 'LB'))
);

CREATE INDEX idx_commercial_rfq_containers_rfq ON commercial_rfq_containers(rfq_id);

CREATE TABLE commercial_quotations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    rfq_id UUID NOT NULL,
    quotation_number VARCHAR(80) NOT NULL,
    revision INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL,
    valid_until TIMESTAMP,
    carrier_name VARCHAR(255),
    transit_time_days INTEGER,
    free_time_days INTEGER,
    estimated_departure TIMESTAMP,
    estimated_arrival TIMESTAMP,
    selling_currency VARCHAR(3) NOT NULL,
    exchange_rate NUMERIC(18,6),
    exchange_rate_date TIMESTAMP,
    exchange_rate_source VARCHAR(100),
    cost_total NUMERIC(18,2) NOT NULL DEFAULT 0,
    selling_total NUMERIC(18,2) NOT NULL DEFAULT 0,
    profit_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    margin_percentage NUMERIC(12,4) NOT NULL DEFAULT 0,
    markup_percentage NUMERIC(12,4) NOT NULL DEFAULT 0,
    commercial_notes TEXT,
    internal_notes TEXT,
    created_by UUID NOT NULL REFERENCES users(id),
    submitted_at TIMESTAMP,
    approved_at TIMESTAMP,
    rejected_at TIMESTAMP,
    expired_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_commercial_quotations_number_revision UNIQUE (tenant_id, quotation_number, revision),
    CONSTRAINT chk_commercial_quotations_status CHECK (
        status IN ('DRAFT', 'READY_FOR_REVIEW', 'APPROVED', 'REJECTED', 'SENT', 'ACCEPTED', 'DECLINED', 'EXPIRED', 'CANCELLED')
    ),
    CONSTRAINT chk_commercial_quotations_revision CHECK (revision >= 1),
    CONSTRAINT chk_commercial_quotations_currency CHECK (char_length(selling_currency) = 3),
    CONSTRAINT chk_commercial_quotations_exchange_rate CHECK (exchange_rate IS NULL OR exchange_rate > 0),
    CONSTRAINT chk_commercial_quotations_transit_time CHECK (transit_time_days IS NULL OR transit_time_days >= 0),
    CONSTRAINT chk_commercial_quotations_free_time CHECK (free_time_days IS NULL OR free_time_days >= 0),
    CONSTRAINT chk_commercial_quotations_cost_total CHECK (cost_total >= 0),
    CONSTRAINT chk_commercial_quotations_selling_total CHECK (selling_total >= 0),
    CONSTRAINT chk_commercial_quotations_valid_until CHECK (valid_until IS NULL OR valid_until >= created_at),
    CONSTRAINT chk_commercial_quotations_eta CHECK (
        estimated_departure IS NULL OR estimated_arrival IS NULL OR estimated_departure <= estimated_arrival
    ),
    CONSTRAINT fk_commercial_quotations_rfq_tenant FOREIGN KEY (rfq_id, tenant_id)
        REFERENCES commercial_rfqs(id, tenant_id)
);

CREATE INDEX idx_commercial_quotations_tenant_status ON commercial_quotations(tenant_id, status);
CREATE INDEX idx_commercial_quotations_tenant_rfq ON commercial_quotations(tenant_id, rfq_id);

CREATE TABLE commercial_quotation_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quotation_id UUID NOT NULL REFERENCES commercial_quotations(id) ON DELETE CASCADE,
    category VARCHAR(40) NOT NULL,
    description VARCHAR(500) NOT NULL,
    scope VARCHAR(30) NOT NULL,
    cost_currency VARCHAR(3) NOT NULL,
    cost_amount NUMERIC(18,2) NOT NULL,
    exchange_rate NUMERIC(18,6),
    cost_amount_in_selling_currency NUMERIC(18,2) NOT NULL,
    selling_currency VARCHAR(3) NOT NULL,
    selling_amount NUMERIC(18,2) NOT NULL,
    quantity NUMERIC(18,3) NOT NULL,
    unit VARCHAR(50),
    total_cost NUMERIC(18,2) NOT NULL,
    total_selling NUMERIC(18,2) NOT NULL,
    profit_amount NUMERIC(18,2) NOT NULL,
    margin_percentage NUMERIC(12,4) NOT NULL,
    markup_percentage NUMERIC(12,4) NOT NULL,
    included BOOLEAN NOT NULL DEFAULT TRUE,
    optional BOOLEAN NOT NULL DEFAULT FALSE,
    supplier_name VARCHAR(255),
    notes TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_commercial_quotation_items_category CHECK (
        category IN (
            'OCEAN_FREIGHT', 'ORIGIN_CHARGE', 'DESTINATION_CHARGE', 'PORT_CHARGE', 'PICKUP',
            'DELIVERY', 'CUSTOMS_CLEARANCE', 'DOCUMENTATION', 'INSURANCE', 'STORAGE',
            'DEMURRAGE', 'DETENTION', 'SECURITY', 'HANDLING', 'OTHER'
        )
    ),
    CONSTRAINT chk_commercial_quotation_items_scope CHECK (scope IN ('ORIGIN', 'MAIN_CARRIAGE', 'DESTINATION', 'GENERAL')),
    CONSTRAINT chk_commercial_quotation_items_cost CHECK (cost_amount >= 0),
    CONSTRAINT chk_commercial_quotation_items_selling CHECK (selling_amount >= 0),
    CONSTRAINT chk_commercial_quotation_items_quantity CHECK (quantity > 0),
    CONSTRAINT chk_commercial_quotation_items_exchange_rate CHECK (exchange_rate IS NULL OR exchange_rate > 0),
    CONSTRAINT chk_commercial_quotation_items_sort_order CHECK (sort_order >= 0),
    CONSTRAINT chk_commercial_quotation_items_currency CHECK (
        char_length(cost_currency) = 3 AND char_length(selling_currency) = 3
    )
);

CREATE INDEX idx_commercial_quotation_items_quotation_sort ON commercial_quotation_items(quotation_id, sort_order);
