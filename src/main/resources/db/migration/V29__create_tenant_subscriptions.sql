CREATE TABLE tenant_subscriptions (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    plan_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP NULL,
    reason VARCHAR(255) NULL,
    internal_notes TEXT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_tenant_subscriptions_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id)
        ON DELETE RESTRICT
        ON UPDATE NO ACTION,
    CONSTRAINT fk_tenant_subscriptions_plan
        FOREIGN KEY (plan_id) REFERENCES subscription_plans(id)
        ON DELETE RESTRICT
        ON UPDATE NO ACTION,
    CONSTRAINT chk_tenant_subscriptions_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CANCELLED')),
    CONSTRAINT chk_tenant_subscriptions_ended_at
        CHECK (ended_at IS NULL OR ended_at >= started_at),
    CONSTRAINT chk_tenant_subscriptions_reason_not_blank
        CHECK (reason IS NULL OR BTRIM(reason) <> ''),
    CONSTRAINT chk_tenant_subscriptions_internal_notes_not_blank
        CHECK (internal_notes IS NULL OR BTRIM(internal_notes) <> '')
);

CREATE INDEX idx_tenant_subscriptions_tenant_id
    ON tenant_subscriptions (tenant_id);

CREATE INDEX idx_tenant_subscriptions_plan_id
    ON tenant_subscriptions (plan_id);

CREATE INDEX idx_tenant_subscriptions_status
    ON tenant_subscriptions (status);

CREATE INDEX idx_tenant_subscriptions_tenant_id_status
    ON tenant_subscriptions (tenant_id, status);

CREATE UNIQUE INDEX uq_tenant_subscriptions_one_open
    ON tenant_subscriptions (tenant_id)
    WHERE ended_at IS NULL AND status IN ('ACTIVE', 'SUSPENDED');

CREATE UNIQUE INDEX uq_tenant_subscriptions_one_active
    ON tenant_subscriptions (tenant_id)
    WHERE status = 'ACTIVE';

CREATE TABLE tenant_subscription_events (
    id UUID PRIMARY KEY,
    tenant_subscription_id UUID NULL,
    tenant_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    previous_plan_id UUID NULL,
    new_plan_id UUID NULL,
    previous_status VARCHAR(30) NULL,
    new_status VARCHAR(30) NULL,
    reason VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_tenant_subscription_events_subscription
        FOREIGN KEY (tenant_subscription_id) REFERENCES tenant_subscriptions(id)
        ON DELETE SET NULL
        ON UPDATE NO ACTION,
    CONSTRAINT fk_tenant_subscription_events_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id)
        ON DELETE RESTRICT
        ON UPDATE NO ACTION,
    CONSTRAINT fk_tenant_subscription_events_previous_plan
        FOREIGN KEY (previous_plan_id) REFERENCES subscription_plans(id)
        ON DELETE RESTRICT
        ON UPDATE NO ACTION,
    CONSTRAINT fk_tenant_subscription_events_new_plan
        FOREIGN KEY (new_plan_id) REFERENCES subscription_plans(id)
        ON DELETE RESTRICT
        ON UPDATE NO ACTION,
    CONSTRAINT chk_tenant_subscription_events_type
        CHECK (event_type IN (
            'SUBSCRIPTION_ASSIGNED',
            'PLAN_CHANGED',
            'SUBSCRIPTION_SUSPENDED',
            'SUBSCRIPTION_REACTIVATED',
            'SUBSCRIPTION_CANCELLED'
        )),
    CONSTRAINT chk_tenant_subscription_events_previous_status
        CHECK (previous_status IS NULL OR previous_status IN ('ACTIVE', 'SUSPENDED', 'CANCELLED')),
    CONSTRAINT chk_tenant_subscription_events_new_status
        CHECK (new_status IS NULL OR new_status IN ('ACTIVE', 'SUSPENDED', 'CANCELLED')),
    CONSTRAINT chk_tenant_subscription_events_reason_not_blank
        CHECK (reason IS NULL OR BTRIM(reason) <> '')
);

CREATE INDEX idx_tenant_subscription_events_tenant_id
    ON tenant_subscription_events (tenant_id);

CREATE INDEX idx_tenant_subscription_events_subscription_id
    ON tenant_subscription_events (tenant_subscription_id);

CREATE INDEX idx_tenant_subscription_events_created_at
    ON tenant_subscription_events (created_at);
