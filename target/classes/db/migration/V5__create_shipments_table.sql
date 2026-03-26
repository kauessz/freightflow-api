CREATE TABLE shipments (
    id UUID PRIMARY KEY,
    booking VARCHAR(255) NOT NULL UNIQUE,
    container_number VARCHAR(11),
    container_type VARCHAR(50),
    voyage_id UUID NOT NULL,
    origin_port_id UUID NOT NULL,
    destination_port_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    consignee VARCHAR(255),
    shipper VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (voyage_id) REFERENCES voyages(id),
    FOREIGN KEY (origin_port_id) REFERENCES ports(id),
    FOREIGN KEY (destination_port_id) REFERENCES ports(id),
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CHECK (status IN ('BOOKED', 'CONFIRMED', 'GATE_IN', 'LOADED', 'IN_TRANSIT', 'ARRIVED', 'GATE_OUT', 'DELIVERED', 'CANCELLED')),
    CHECK (container_type IN ('TEU20', 'TEU40', 'TEU40HC', 'REEFER20', 'REEFER40') OR container_type IS NULL)
);

CREATE INDEX idx_shipment_booking ON shipments(booking);
CREATE INDEX idx_shipment_status ON shipments(status);
CREATE INDEX idx_shipment_tenant ON shipments(tenant_id);
CREATE INDEX idx_shipment_voyage ON shipments(voyage_id);
