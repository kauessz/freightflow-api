CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    tenant_id UUID NOT NULL,
    active BOOLEAN NOT NULL,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CHECK (role IN ('ADMIN', 'USER', 'VIEWER'))
);

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_tenant ON users(tenant_id);

CREATE TABLE api_keys (
    id UUID PRIMARY KEY,
    key_hash VARCHAR(255) NOT NULL UNIQUE,
    key_prefix VARCHAR(8) NOT NULL,
    name VARCHAR(255) NOT NULL,
    tenant_id UUID NOT NULL,
    created_by_id UUID NOT NULL,
    active BOOLEAN NOT NULL,
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (created_by_id) REFERENCES users(id)
);

CREATE INDEX idx_api_key_tenant ON api_keys(tenant_id);
CREATE INDEX idx_api_key_active ON api_keys(active);
