CREATE TABLE vessels (
    id UUID PRIMARY KEY,
    imo VARCHAR(7) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    flag VARCHAR(2) NOT NULL,
    type VARCHAR(50) NOT NULL,
    capacity_teu INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CHECK (type IN ('CONTAINER', 'BULK', 'TANKER', 'RORO'))
);

CREATE INDEX idx_vessel_imo ON vessels(imo);
