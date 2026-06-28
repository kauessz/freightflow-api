DO $$
DECLARE
    demo_tenant_id UUID;
    demo_customer_id UUID := 'c0000001-0000-0000-0000-000000000001';
    demo_password_hash TEXT := '$2a$10$B6m3U6XOAUahJ03W.fqwv.H6BbAAN3InhXZUeMqHr4c1DSRDNbgPS';
BEGIN
    IF current_database() <> 'freightflow_dev' THEN
        RETURN;
    END IF;

    SELECT id
    INTO demo_tenant_id
    FROM tenants
    WHERE slug = 'freightflow-demo'
    LIMIT 1;

    IF demo_tenant_id IS NULL THEN
        RETURN;
    END IF;

    INSERT INTO users (id, tenant_id, customer_id, email, password_hash, name, role, active, created_at, updated_at)
    VALUES (
        'e0000001-0000-0000-0000-000000000001',
        demo_tenant_id,
        NULL,
        'demo@freightflow.io',
        demo_password_hash,
        'Demo Admin',
        'ADMIN',
        TRUE,
        NOW(),
        NOW()
    )
    ON CONFLICT (email) DO UPDATE
    SET tenant_id = EXCLUDED.tenant_id,
        customer_id = EXCLUDED.customer_id,
        password_hash = EXCLUDED.password_hash,
        name = EXCLUDED.name,
        role = EXCLUDED.role,
        active = TRUE,
        updated_at = NOW();

    INSERT INTO users (id, tenant_id, customer_id, email, password_hash, name, role, active, created_at, updated_at)
    VALUES (
        'e0000002-0000-0000-0000-000000000002',
        demo_tenant_id,
        NULL,
        'operador@freightflow.io',
        demo_password_hash,
        'Operador Demo',
        'OPERATOR',
        TRUE,
        NOW(),
        NOW()
    )
    ON CONFLICT (email) DO UPDATE
    SET tenant_id = EXCLUDED.tenant_id,
        customer_id = EXCLUDED.customer_id,
        password_hash = EXCLUDED.password_hash,
        name = EXCLUDED.name,
        role = EXCLUDED.role,
        active = TRUE,
        updated_at = NOW();

    INSERT INTO users (id, tenant_id, customer_id, email, password_hash, name, role, active, created_at, updated_at)
    VALUES (
        'e0000003-0000-0000-0000-000000000003',
        demo_tenant_id,
        NULL,
        'viewer@freightflow.io',
        demo_password_hash,
        'Viewer Demo',
        'VIEWER',
        TRUE,
        NOW(),
        NOW()
    )
    ON CONFLICT (email) DO UPDATE
    SET tenant_id = EXCLUDED.tenant_id,
        customer_id = EXCLUDED.customer_id,
        password_hash = EXCLUDED.password_hash,
        name = EXCLUDED.name,
        role = EXCLUDED.role,
        active = TRUE,
        updated_at = NOW();

    INSERT INTO users (id, tenant_id, customer_id, email, password_hash, name, role, active, created_at, updated_at)
    VALUES (
        'e0000004-0000-0000-0000-000000000004',
        demo_tenant_id,
        demo_customer_id,
        'cliente@atlascargo.com',
        demo_password_hash,
        'Cliente Atlas Cargo',
        'CLIENT',
        TRUE,
        NOW(),
        NOW()
    )
    ON CONFLICT (email) DO UPDATE
    SET tenant_id = EXCLUDED.tenant_id,
        customer_id = EXCLUDED.customer_id,
        password_hash = EXCLUDED.password_hash,
        name = EXCLUDED.name,
        role = EXCLUDED.role,
        active = TRUE,
        updated_at = NOW();

    INSERT INTO users (id, tenant_id, customer_id, email, password_hash, name, role, active, created_at, updated_at)
    VALUES (
        'e0000005-0000-0000-0000-000000000005',
        demo_tenant_id,
        NULL,
        'kaue@freightflow.com',
        demo_password_hash,
        'Kaue Admin',
        'ADMIN',
        TRUE,
        NOW(),
        NOW()
    )
    ON CONFLICT (email) DO UPDATE
    SET tenant_id = EXCLUDED.tenant_id,
        customer_id = EXCLUDED.customer_id,
        password_hash = EXCLUDED.password_hash,
        name = EXCLUDED.name,
        role = EXCLUDED.role,
        active = TRUE,
        updated_at = NOW();
END $$;
