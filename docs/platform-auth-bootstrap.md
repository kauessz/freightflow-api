# Platform Authentication Bootstrap

## Purpose

Phase A introduces a platform-only identity that is fully separate from tenant users.

- Platform login: `POST /api/v1/platform/auth/login`
- Current platform user: `GET /api/v1/platform/me`
- Platform JWT is not accepted on tenant endpoints.
- Tenant JWT is not accepted on platform endpoints.

## Required environment variables

Use a secret distinct from the tenant JWT secret.

```env
PLATFORM_JWT_SECRET=change-me-to-a-different-strong-secret-minimum-32-bytes!!
PLATFORM_JWT_ISSUER=freightflow-platform
PLATFORM_JWT_AUDIENCE=freightflow-platform-api
PLATFORM_JWT_EXPIRATION=3600000
```

## One-time bootstrap

Bootstrap is disabled by default and only runs when explicitly enabled.

```env
FREIGHTFLOW_PLATFORM_BOOTSTRAP_ENABLED=true
FREIGHTFLOW_PLATFORM_BOOTSTRAP_EMAIL=platform-admin@example.com
FREIGHTFLOW_PLATFORM_BOOTSTRAP_PASSWORD=ChangeMe123!
```

Behavior:

- runs only when `enabled=true`;
- fails clearly if email or password is missing;
- creates the first `platform_user` only during the one-time bootstrap window;
- writes a persistent bootstrap-completed marker so a later restart does not recreate a removed account automatically;
- stores the password with BCrypt;
- does not write the password to logs;
- should still be disabled again immediately after the first successful startup.

## Local usage

PowerShell example:

```powershell
$env:PLATFORM_JWT_SECRET='change-me-to-a-different-strong-secret-minimum-32-bytes!!'
$env:FREIGHTFLOW_PLATFORM_BOOTSTRAP_ENABLED='true'
$env:FREIGHTFLOW_PLATFORM_BOOTSTRAP_EMAIL='platform-admin@example.com'
$env:FREIGHTFLOW_PLATFORM_BOOTSTRAP_PASSWORD='ChangeMe123!'
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

After the first platform admin is created, turn bootstrap off:

```powershell
$env:FREIGHTFLOW_PLATFORM_BOOTSTRAP_ENABLED='false'
```

## Credential rotation

- rotate `PLATFORM_JWT_SECRET` independently of `JWT_SECRET`;
- update the platform admin password through a future platform-management flow;
- avoid reusing tenant credentials for platform users.

## Security notes

- platform users do not have `tenantId`;
- platform tokens do not include `customerId`;
- platform identities do not grant operational access to shipments, documents, commercial flows, or tenant data.
