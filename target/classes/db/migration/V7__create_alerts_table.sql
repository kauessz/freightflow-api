CREATE TABLE alerts (
    id UUID PRIMARY KEY,
    shipment_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    message VARCHAR(500) NOT NULL,
    resolved BOOLEAN NOT NULL,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (shipment_id) REFERENCES shipments(id),
    CHECK (type IN ('DELAY', 'ROUTE_DEVIATION', 'DEMURRAGE_RISK', 'CUSTOMS_HOLD', 'EQUIPMENT_ISSUE')),
    CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

CREATE INDEX idx_alert_shipment ON alerts(shipment_id);
CREATE INDEX idx_alert_resolved ON alerts(resolved);
