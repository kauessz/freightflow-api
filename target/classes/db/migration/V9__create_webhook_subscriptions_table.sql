CREATE TABLE webhook_subscriptions (
    id UUID PRIMARY KEY,
    url VARCHAR(500) NOT NULL,
    secret VARCHAR(255) NOT NULL,
    events TEXT NOT NULL,
    tenant_id UUID NOT NULL,
    active BOOLEAN NOT NULL,
    last_triggered_at TIMESTAMP,
    failure_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

CREATE INDEX idx_webhook_tenant ON webhook_subscriptions(tenant_id);
CREATE INDEX idx_webhook_active ON webhook_subscriptions(active);
