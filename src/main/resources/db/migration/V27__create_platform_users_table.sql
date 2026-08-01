CREATE TABLE platform_users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_platform_users_role
        CHECK (role IN ('PLATFORM_ADMIN')),
    CONSTRAINT chk_platform_users_status
        CHECK (status IN ('ACTIVE', 'DISABLED')),
    CONSTRAINT chk_platform_users_email_not_blank
        CHECK (BTRIM(email) <> ''),
    CONSTRAINT chk_platform_users_email_normalized
        CHECK (email = LOWER(BTRIM(email))),
    CONSTRAINT chk_platform_users_password_hash_not_blank
        CHECK (BTRIM(password_hash) <> ''),
    CONSTRAINT uq_platform_users_email
        UNIQUE (email)
);

CREATE TABLE platform_bootstrap_state (
    bootstrap_key VARCHAR(100) PRIMARY KEY,
    completed_at TIMESTAMP NOT NULL,
    platform_user_id UUID,
    CONSTRAINT fk_platform_bootstrap_state_user
        FOREIGN KEY (platform_user_id) REFERENCES platform_users(id)
        ON DELETE SET NULL
        ON UPDATE NO ACTION
);
