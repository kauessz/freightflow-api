CREATE TABLE ports (
    id UUID PRIMARY KEY,
    unlocode VARCHAR(5) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    country VARCHAR(2) NOT NULL,
    timezone VARCHAR(50) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_port_unlocode ON ports(unlocode);
CREATE INDEX idx_port_country ON ports(country);
