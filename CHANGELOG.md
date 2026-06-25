# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-06-07

### Added

#### Authentication & Security
- JWT authentication with HS256 signing (configurable expiration via `JWT_EXPIRATION`)
- Role-based access control (RBAC) with four roles: `ADMIN`, `OPERATOR`, `VIEWER`, `CLIENT`
- `@RequiresRole` annotation with `RoleCheckAspect` for declarative role enforcement on controller methods
- `UserPrincipal` integration with Spring Security for multi-tenant identity propagation
- `POST /api/v1/auth/register` — register a new user
- `POST /api/v1/auth/login` — authenticate and receive JWT
- `POST /api/v1/auth/refresh` — refresh access token
- `GET /api/v1/auth/me` — get authenticated user info

#### Multi-Tenancy
- Full tenant isolation on all domain entities via `tenantId` foreign key
- `CLIENT` role further scoped to a single `customerId` — cannot see other customers' data
- Repository-level query filtering (`findByIdAndTenantId`, `findByTenantId`, etc.) throughout all modules

#### Shipments Module
- `GET /api/v1/shipments` — paginated list with filters (`status`, `riskLevel`, `voyageId`, `customerId`); `CLIENT` filtered by own customer
- `GET /api/v1/shipments/stats` — KPI counts: total, in-transit, arrived, delayed, at-risk
- `GET /api/v1/shipments/{id}` — full shipment detail with relations (voyage, ports, events, alerts)
- `POST /api/v1/shipments` — create shipment
- `PUT /api/v1/shipments/{id}` — update shipment
- `DELETE /api/v1/shipments/{id}` — soft-delete shipment

#### Public Tracking
- `GET /api/v1/tracking/{booking}` — unauthenticated endpoint; returns voyage, vessel position, and event history by booking number

#### Voyages Module
- `GET /api/v1/voyages` — paginated list with filters (`status`, `vesselId`, `originPortId`, `destPortId`)
- `GET /api/v1/voyages/fleet-map-readiness` — Fleet Map eligibility status per voyage for the caller's tenant; supports `?eligible=true/false` filter
- `GET /api/v1/voyages/number/{voyageNumber}` — voyage lookup by voyage number (e.g. `MSC-2026-001`)
- `GET /api/v1/voyages/{id}` — voyage detail
- `GET /api/v1/voyages/{id}/shipments` — tenant's shipments on a voyage; `CLIENT` filtered by `customerId`
- `GET /api/v1/voyages/{id}/tracking` — live vessel position + voyage data (delegates to AIS resolution chain)
- `POST /api/v1/voyages` — create voyage
- `PUT /api/v1/voyages/{id}` — update voyage
- `DELETE /api/v1/voyages/{id}` — delete voyage (rejected if any shipments are linked)

#### Vessels Module
- `GET /api/v1/vessels` — list all vessels
- `GET /api/v1/vessels/active-with-shipments` — active (`IN_TRANSIT` / `DEPARTED`) voyages containing the caller's shipments, enriched with AIS position and Fleet Map eligibility; CLIENT-scoped
- `GET /api/v1/vessels/{id}` — vessel by ID
- `GET /api/v1/vessels/imo/{imo}` — vessel by IMO number
- `GET /api/v1/vessels/{imo}/position` — current AIS position via `VesselPositionResolver`
- `POST /api/v1/vessels` — register vessel
- `PUT /api/v1/vessels/{id}` — update vessel
- `DELETE /api/v1/vessels/{id}` — delete vessel (rejected if any voyages are linked)

#### Ports Module
- `GET /api/v1/ports` — list ports
- `GET /api/v1/ports/{id}` — port by ID
- `GET /api/v1/ports/unlocode/{unlocode}` — port by UNLOCODE (e.g. `BRSSZ`)
- `GET /api/v1/ports/country/{country}` — ports filtered by two-letter country code (e.g. `BR`)
- `GET /api/v1/ports/search` — full-text search by port name or UNLOCODE
- `POST /api/v1/ports` — register port
- `PUT /api/v1/ports/{id}` — update port

#### Customers Module
- `GET /api/v1/customers` — list customers
- `GET /api/v1/customers/{id}` — customer by ID
- `POST /api/v1/customers` — create customer
- `PUT /api/v1/customers/{id}` — update customer
- `DELETE /api/v1/customers/{id}` — delete customer

#### Users Module
- `GET /api/v1/users` — list users (ADMIN only)
- `GET /api/v1/users/{id}` — user by ID
- `POST /api/v1/users` — create user
- `PUT /api/v1/users/{id}` — update user
- `DELETE /api/v1/users/{id}` — delete user

#### Events Module
- `GET /api/v1/shipments/{id}/events` — event history for a shipment (chronological ASC order)
- `GET /api/v1/shipments/{id}/events/{eventId}` — single event detail
- `POST /api/v1/shipments/{id}/events` — register a new event (triggers alert evaluation)
- `DELETE /api/v1/shipments/{id}/events/{eventId}` — delete event

#### Alerts Module
- `GET /api/v1/alerts` — list open alerts for the caller's tenant
- `GET /api/v1/shipments/{id}/alerts` — all alerts (open + resolved) for a shipment
- `POST /api/v1/alerts` — create alert; prevents duplicate open alerts of the same type per shipment
- `POST /api/v1/alerts/{id}/resolve` — resolve an alert

#### Import Module
- `POST /api/v1/import/shipments` — bulk import shipments via CSV (`multipart/form-data`)
- `GET /api/v1/import/template` — download the CSV import template
- `GET /api/v1/import/formats` — list supported import formats and field descriptions

#### Fleet Map Feature (P2 — A1)
- `VoyageFleetMapEligibilityService` — evaluates voyage eligibility for the Fleet Map; produces structured `FleetMapEligibilityResult` with a boolean flag and typed `FleetMapIneligibilityReason` list
- `VesselPositionResolver` — three-tier AIS resolution chain: live AIS → midpoint estimate → `UNAVAILABLE`; integrated on `VesselWithVoyageResponse`
- `VesselWithVoyageResponse` DTO — enriched vessel card with voyage context, AIS position, shipment count, Fleet Map eligibility, and ineligibility reasons

#### Database Migrations (Flyway V1–V18)
- `V1` — core schema: tenants, users, customers, vessels, ports, voyages, shipments
- `V2` — events table
- `V3` — alerts table
- `V4` — import_jobs table
- `V5` — add `bl_status`, `customs_status`, `risk_level` to shipments
- `V6` — add `voyage_number` unique index on voyages
- `V7` — add `alert_type` and `severity` to alerts; add `resolved_at`, `resolved_by`
- `V8` — add `container_type` to shipments
- `V9` — add `carrier` to vessels
- `V10` — seed 40+ real UNLOCODE ports (Santos, Buenos Aires, Montevideo, Paranaguá, etc.)
- `V11` — add `imo` unique index to vessels
- `V12` — seed 5 test vessels with real IMO numbers
- `V13` — seed test voyages on Santos–Buenos Aires–Montevideo corridor
- `V14` — seed shipments linked to test voyages with realistic booking numbers
- `V15` — seed events and alerts for seeded shipments
- `V16` — add `customer_id` FK to users (CLIENT binding)
- `V17` — fix BCrypt hashes for seed users (`operador`, `viewer`, `cliente`)
- `V18` — add `active`/`updated_at` to ports, `active` to vessels and voyages; master data readiness

#### Infrastructure & CI/CD
- Docker multi-stage build (`docker/Dockerfile`) — builder stage with Maven, runtime stage with Eclipse Temurin 21 JRE Alpine
- `docker-compose.yml` — orchestrates API, PostgreSQL 16, Redis 7, and RabbitMQ 3
- GitHub Actions CI pipeline — three sequential jobs: `test` → `build` → `docker push to GHCR`
- Codecov integration via `codecov/codecov-action@v4` for coverage tracking
- SpringDoc OpenAPI 3 — Swagger UI available at `/swagger-ui.html`
- `PageResponse<T>` — standardized pagination wrapper: `{ data, meta: { total, page, size, totalPages } }`
- RFC 7807 `ProblemDetail` — uniform error responses via `GlobalExceptionHandler`
- Actuator endpoints: `health`, `info`, `metrics` (details require `ACTUATOR_ADMIN` role in prod)

#### Testing
- 149 tests across unit and integration layers (0 failures)
- Unit tests: service layer with Mockito 5 strict stubs (`@ExtendWith(MockitoExtension.class)`)
- Integration tests: full HTTP stack via Testcontainers 1.20.4 (`PostgreSQLContainer`)
- JaCoCo coverage reports generated on every `mvn verify`

[Unreleased]: https://github.com/kauessz/freightflow-api/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/kauessz/freightflow-api/releases/tag/v0.1.0
