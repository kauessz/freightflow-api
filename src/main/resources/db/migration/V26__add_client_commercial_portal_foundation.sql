ALTER TABLE users
    DROP CONSTRAINT users_customer_id_fkey;

ALTER TABLE customers
    ADD CONSTRAINT uq_customers_id_tenant UNIQUE (id, tenant_id);

ALTER TABLE users
    ADD CONSTRAINT uq_users_id_tenant UNIQUE (id, tenant_id);

ALTER TABLE users
    ADD CONSTRAINT fk_users_customer_tenant
        FOREIGN KEY (customer_id, tenant_id)
        REFERENCES customers(id, tenant_id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;

ALTER TABLE commercial_quotations
    ADD COLUMN sent_at TIMESTAMP,
    ADD COLUMN approved_by UUID,
    ADD COLUMN sent_by UUID;

ALTER TABLE commercial_quotations
    ADD CONSTRAINT fk_commercial_quotations_approved_by_tenant
        FOREIGN KEY (approved_by, tenant_id)
        REFERENCES users(id, tenant_id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;

ALTER TABLE commercial_quotations
    ADD CONSTRAINT fk_commercial_quotations_sent_by_tenant
        FOREIGN KEY (sent_by, tenant_id)
        REFERENCES users(id, tenant_id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;

CREATE INDEX idx_commercial_rfqs_tenant_customer_status_created_at
    ON commercial_rfqs(tenant_id, customer_id, status, created_at DESC);

DROP INDEX IF EXISTS idx_commercial_quotations_tenant_status;

CREATE INDEX idx_commercial_quotations_tenant_status_sent_at
    ON commercial_quotations(tenant_id, status, sent_at DESC);

CREATE INDEX idx_users_tenant_customer_role
    ON users(tenant_id, customer_id, role);
