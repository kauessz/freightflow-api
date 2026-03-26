CREATE TABLE voyages (
    id UUID PRIMARY KEY,
    voyage_number VARCHAR(255) NOT NULL UNIQUE,
    vessel_id UUID NOT NULL,
    origin_port_id UUID NOT NULL,
    destination_port_id UUID NOT NULL,
    etd TIMESTAMP NOT NULL,
    eta TIMESTAMP NOT NULL,
    atd TIMESTAMP,
    ata TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (vessel_id) REFERENCES vessels(id),
    FOREIGN KEY (origin_port_id) REFERENCES ports(id),
    FOREIGN KEY (destination_port_id) REFERENCES ports(id),
    CHECK (status IN ('SCHEDULED', 'DEPARTED', 'IN_TRANSIT', 'ARRIVED', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_voyage_number ON voyages(voyage_number);
CREATE INDEX idx_voyage_status ON voyages(status);
CREATE INDEX idx_voyage_vessel ON voyages(vessel_id);
