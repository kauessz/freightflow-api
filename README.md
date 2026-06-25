# FreightFlow API

> Maritime shipment management and tracking REST API — built for freight forwarders and logistics operators.

[![CI](https://github.com/kauessz/freightflow-api/actions/workflows/ci.yml/badge.svg)](https://github.com/kauessz/freightflow-api/actions/workflows/ci.yml)
[![Coverage](https://codecov.io/gh/kauessz/freightflow-api/branch/main/graph/badge.svg)](https://codecov.io/gh/kauessz/freightflow-api)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Tests](https://img.shields.io/badge/Tests-59_passing-2ea44f)](#testing)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## Overview

FreightFlow API is a production-ready backend for tracking maritime shipments across ports, voyages, and vessels. It provides a public tracking endpoint by booking number, a full CRUD management layer protected by JWT authentication, real-time event logging per shipment, automated alert generation (delays, demurrage risk, customs holds), and bulk CSV import for shipments and voyages.

The domain is modeled after real operations in the **Santos–Buenos Aires corridor** (Mercosul Line / CMA CGM Group routes), covering port scheduling, ETA calculation, and container tracking by UNLOCODE.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 (LTS) |
| Framework | Spring Boot 3.3 |
| Build | Maven 3.9 |
| Database | PostgreSQL 16 |
| ORM / Migrations | Spring Data JPA + Flyway |
| Security | Spring Security + JWT |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| Cache | Redis |
| Messaging | RabbitMQ |
| Testing | JUnit 5 + Testcontainers + Mockito |
| Coverage | JaCoCo |
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions |

---

## Architecture

```
src/main/java/com/freightflow/api/
├── config/             # Security, CORS, OpenAPI, Redis config
├── domain/
│   ├── entity/         # JPA entities: Shipment, Voyage, Vessel, Port, Event, Alert
│   └── enums/          # ShipmentStatus, VoyageStatus, EventType, AlertType...
├── dto/                # Request/Response DTOs with Bean Validation
├── repository/         # Spring Data JPA repositories
├── service/            # Business logic layer
├── controller/         # REST controllers (versioned under /api/v1)
├── exception/          # GlobalExceptionHandler, custom exceptions
├── messaging/          # RabbitMQ producers/consumers
└── util/               # ETA calculator, pagination helpers
```

The project follows a layered architecture with strict separation between controller, service, and repository. Business logic lives exclusively in the service layer — controllers only handle HTTP concerns.

---

## Domain Model

```
Vessel ──< Voyage >── Port (origin/destination)
              │
           Shipment
              │
          ├── Event[]    ← gate-in, loaded, departed, arrived, customs...
          └── Alert[]    ← delay, demurrage risk, equipment issue...
```

Key domain identifiers:
- **IMO number** — unique vessel identifier (e.g. `9321483`)
- **UNLOCODE** — port code per UN standard (e.g. `BRSSZ` = Santos, `ARBUE` = Buenos Aires)
- **Booking number** — shipment reference for public tracking (e.g. `P10482561`)
- **Container number** — ISO format (e.g. `TCLU1234567`)

---

## API Reference

All authenticated routes require `Authorization: Bearer <token>`.

### Auth
```
POST /api/v1/auth/register    Register a new user
POST /api/v1/auth/login       Authenticate and receive JWT
POST /api/v1/auth/refresh     Refresh access token
GET  /api/v1/auth/me          Get authenticated user info
```

### Users
```
GET    /api/v1/users          List users (ADMIN only)
GET    /api/v1/users/{id}     Get user by ID
POST   /api/v1/users          Create user
PUT    /api/v1/users/{id}     Update user
DELETE /api/v1/users/{id}     Delete user
```

### Shipments
```
GET    /api/v1/shipments           List with pagination (CLIENT sees own shipments only)
GET    /api/v1/shipments/stats     KPI counts: total, in-transit, arrived, delayed, at-risk
GET    /api/v1/shipments/{id}      Shipment details with relations
POST   /api/v1/shipments           Create shipment
PUT    /api/v1/shipments/{id}      Update shipment
DELETE /api/v1/shipments/{id}      Remove shipment
```

### Public Tracking
```
GET /api/v1/tracking/{booking}     Public tracking by booking number (no auth required)
```

### Voyages
```
GET    /api/v1/voyages                          List voyages
GET    /api/v1/voyages/fleet-map-readiness      Fleet Map eligibility status per voyage
GET    /api/v1/voyages/number/{voyageNumber}    Voyage by voyage number (e.g. MSC-2026-001)
GET    /api/v1/voyages/{id}                     Voyage details
GET    /api/v1/voyages/{id}/shipments           Tenant's shipments on a voyage (CLIENT filtered by customer)
GET    /api/v1/voyages/{id}/tracking            Live vessel position + voyage data (AIS)
POST   /api/v1/voyages                          Create voyage
PUT    /api/v1/voyages/{id}                     Update voyage
DELETE /api/v1/voyages/{id}                     Delete voyage (only if no shipments)
```

### Vessels
```
GET    /api/v1/vessels                          List vessels
GET    /api/v1/vessels/active-with-shipments    Active voyages containing tenant's shipments (Fleet Map)
GET    /api/v1/vessels/{id}                     Vessel by ID
GET    /api/v1/vessels/imo/{imo}                Vessel by IMO number
GET    /api/v1/vessels/{imo}/position           Current AIS position for a vessel
POST   /api/v1/vessels                          Register vessel
PUT    /api/v1/vessels/{id}                     Update vessel
DELETE /api/v1/vessels/{id}                     Delete vessel (only if no voyages)
```

### Ports
```
GET    /api/v1/ports                    List ports
GET    /api/v1/ports/{id}               Port by ID
GET    /api/v1/ports/unlocode/{unlocode} Port by UNLOCODE (e.g. BRSSZ)
GET    /api/v1/ports/country/{country}  Ports by country code (e.g. BR)
GET    /api/v1/ports/search             Full-text search by name or UNLOCODE
POST   /api/v1/ports                    Register port
PUT    /api/v1/ports/{id}               Update port
```

### Customers
```
GET    /api/v1/customers          List customers
GET    /api/v1/customers/{id}     Customer by ID
POST   /api/v1/customers          Create customer
PUT    /api/v1/customers/{id}     Update customer
DELETE /api/v1/customers/{id}     Delete customer
```

### Events
```
GET    /api/v1/shipments/{id}/events             Event history for shipment (ASC order)
GET    /api/v1/shipments/{id}/events/{eventId}   Single event
POST   /api/v1/shipments/{id}/events             Register event on shipment
DELETE /api/v1/shipments/{id}/events/{eventId}   Delete event
```

### Alerts
```
GET    /api/v1/alerts                      List open alerts for the caller's tenant
GET    /api/v1/shipments/{id}/alerts       All alerts (open + resolved) for a shipment
POST   /api/v1/alerts                      Create alert (prevents duplicate open alerts of same type)
POST   /api/v1/alerts/{id}/resolve         Resolve an alert
```

### Import
```
POST   /api/v1/import/shipments   Bulk import shipments via CSV file (multipart/form-data)
GET    /api/v1/import/template    Download CSV template for shipments
GET    /api/v1/import/formats     List supported import formats and field descriptions
```

Full interactive documentation is available at `/swagger-ui.html` when running locally.

---

## Getting Started

### Prerequisites

- Java 21+
- Docker and Docker Compose
- Maven 3.9+

### Running with Docker Compose

```bash
# Clone the repository
git clone https://github.com/kauessz/freightflow-api.git
cd freightflow-api

# Copy environment variables
cp .env.example .env
# Edit .env and set JWT_SECRET (min 32 characters)

# Start all services (PostgreSQL, Redis, RabbitMQ, API)
docker compose up -d

# Run database migrations
./mvnw flyway:migrate

# Seed with sample port data (UNLOCODE dataset)
./mvnw spring-boot:run -Dspring-boot.run.arguments=--seed
```

### Running locally with the `dev` profile

Document storage supports two local modes:

- Mock storage (default for local dev): leave `STORAGE_ENDPOINT` unset or blank.
- S3-compatible storage (Cloudflare R2 / MinIO): set `STORAGE_ENDPOINT` to a full `http://` or `https://` URL and provide bucket/credentials by env.
- RabbitMQ messaging is optional in local dev by default: `FREIGHTFLOW_MESSAGING_ENABLED=false`.
- To exercise real AMQP flows locally, start a broker and set `FREIGHTFLOW_MESSAGING_ENABLED=true`.

Example using mock storage:

```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Example using S3-compatible storage:

```bash
STORAGE_ENDPOINT=http://localhost:9000
STORAGE_BUCKET=freightflow-docs
STORAGE_ACCESS_KEY_ID=local-access-key
STORAGE_SECRET_ACCESS_KEY=local-secret-key
STORAGE_PUBLIC_BASE_URL=http://localhost:8080/mock-storage
FREIGHTFLOW_MESSAGING_ENABLED=true

mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

If `STORAGE_ENDPOINT` is set, it must include the URI scheme. Values like `localhost:9000` or `r2.example.com` are rejected during startup to avoid ambiguous or broken storage wiring.
When `FREIGHTFLOW_MESSAGING_ENABLED=false`, the API starts without RabbitMQ listeners and `/actuator/health` does not report Rabbit as `DOWN`.

API will be available at: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`

### Running locally (without Docker for the app)

```bash
# Start only infrastructure services
docker compose up -d postgres redis rabbitmq

# Set environment variables
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/freightflow"
$env:JWT_SECRET="your-secret-key-minimum-32-characters"

# Run the application
./mvnw spring-boot:run
```

### Environment Variables

| Variable | Description | Example |
|---|---|---|
| `DATABASE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/freightflow` |
| `JWT_SECRET` | JWT signing key (min 32 chars) | `change-me-in-production-xxxxx` |
| `JWT_EXPIRATION` | Token expiration in ms | `86400000` (24h) |
| `REDIS_URL` | Redis connection URL | `redis://localhost:6379` |
| `RABBITMQ_URL` | RabbitMQ AMQP URL | `amqp://guest:guest@localhost:5672` |
| `SPRING_PROFILES_ACTIVE` | Active profile | `development` / `production` |

---

## Testing

```bash
# Run all tests
./mvnw test

# Run with coverage report (generates target/site/jacoco/index.html)
./mvnw verify
```

**Current test results: 59 tests passing, 0 failures across 71 classes.**

Test strategy:
- **Unit tests** — Service layer with Mockito mocks for repositories
- **Integration tests** — Full HTTP stack with Testcontainers (real PostgreSQL)
- **Coverage** — JaCoCo reports generated on every `mvn verify`

---

## CSV Bulk Import

Shipments and voyages can be imported in bulk via the `/import/csv` endpoints.

Sample CSV format for shipments:
```csv
booking,containerNumber,containerType,voyageNumber,originUnlocode,destUnlocode,consignee,shipper
P10482561,TCLU1234567,TEU40HC,0PLATS1MA,BRSSZ,ARBUE,ACME Corp,Shipper SA
P10482562,,TEU20,0PLATS1MA,BRSSZ,UYMVD,Beta Ltd,Shipper SA
```

---

## Project Context

This project was built as a portfolio piece to demonstrate backend engineering depth in the **LogTech domain**. The author has 15+ years of operational experience in multimodal port and rail logistics (Mercosul Line / CMA CGM Group, Santos-SP, Brazil), which directly influenced the domain modeling decisions — vessel IMO numbers, UNLOCODE port identifiers, booking/container formats, and voyage status flows all reflect real industry practices.

Target companies: Maersk Tech, project44, Flexport, Shippeo, Descartes.

---

## Author

**Kauê Lima**  
Logistics professional → Software Engineer  
[![LinkedIn](https://img.shields.io/badge/LinkedIn-kaue--lima01-0A66C2?logo=linkedin)](https://linkedin.com/in/kaue-lima01)
[![GitHub](https://img.shields.io/badge/GitHub-kauessz-181717?logo=github)](https://github.com/kauessz)
[![Portfolio](https://img.shields.io/badge/Portfolio-kaue--landing.netlify.app-00C7B7)](https://kaue-landing.netlify.app)

---

## License

MIT — see [LICENSE](LICENSE) for details.
