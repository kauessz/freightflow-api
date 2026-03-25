CREATE TABLE events (
    id UUID PRIMARY KEY,
    shipment_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    location VARCHAR(255) NOT NULL,
    description TEXT,
    occurred_at TIMESTAMP NOT NULL,
    reported_at TIMESTAMP NOT NULL,
    FOREIGN KEY (shipment_id) REFERENCES shipments(id),
    CHECK (type IN ('GATE_IN', 'LOADED', 'DEPARTED', 'TRANSSHIPMENT', 'ARRIVED', 'GATE_OUT', 'CUSTOMS_HOLD', 'CUSTOMS_RELEASE'))
);

CREATE INDEX idx_event_shipment_occurred ON events(shipment_id, occurred_at);
