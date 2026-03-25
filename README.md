# FreightFlow API

[![CI](https://github.com/your-username/freightflow-api/actions/workflows/ci.yml/badge.svg)](https://github.com/your-username/freightflow-api/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> Maritime shipment management REST API — real-time tracking, ETA prediction, proactive alerts, and operational analytics for freight forwarders and shippers.

## Overview

FreightFlow is a production-grade REST API designed for managing ocean freight shipments. Built with domain expertise from 15+ years in multimodal port/rail logistics, it provides:

- **Real-time container and cargo tracking** with event-based status updates
- **ETA prediction** based on historical transit data and route analysis
- **Proactive alerts** — delay detection, demurrage risk, customs hold notifications
- **Webhook integrations** for ERP/TMS connectivity
- **Operational analytics** — transit time benchmarks, delay percentages, KPIs
- **Multi-tenant architecture** for multiple clients/shippers

## Tech Stack

| Layer          | Technology                              |
|----------------|-----------------------------------------|
| Runtime        | Java 21 (LTS)                           |
| Framework      | Spring Boot 3.3.x                       |
| Build          | Maven                                   |
| Database       | PostgreSQL 16                           |
| Migrations     | Flyway                                  |
| ORM            | Spring Data JPA + Hibernate             |
| Validation     | Jakarta Bean Validation 3.0             |
| Auth           | Spring Security 6 + JWT (jjwt)          |
| API Docs       | SpringDoc OpenAPI 3 (Swagger UI)        |
| Cache          | Redis (Spring Data Redis)               |
| Queue          | RabbitMQ (Spring AMQP)                  |
| Tests          | JUnit 5 + Mockito + Testcontainers      |
| Containers     | Docker + docker-compose                 |
| CI/CD          | GitHub Actions                          |
| Deploy         | Railway                                 |
| Billing        | Stripe Java SDK                         |

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose

### Quick Start

```bash
# 1. Clone the repository
git clone https://github.com/your-username/freightflow-api.git
cd freightflow-api

# 2. Start infrastructure (PostgreSQL, Redis, RabbitMQ)
docker compose up -d postgres redis rabbitmq

# 3. Copy environment variables
cp .env.example .env

# 4. Run the application
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.
Swagger UI at `http://localhost:8080/swagger-ui.html`.

### Running with Docker (full stack)

```bash
docker compose up -d
```

### Running Tests

```bash
# Unit tests
./mvnw test

# Integration tests (requires Docker for Testcontainers)
./mvnw verify
```

## API Endpoints

### Public

| Method | Endpoint                          | Description            |
|--------|-----------------------------------|------------------------|
| GET    | `/api/v1/tracking/{booking}`      | Public shipment tracking |
| GET    | `/health`                         | Health check           |
| GET    | `/swagger-ui.html`                | API documentation      |

### Shipments (authenticated)

| Method | Endpoint                              | Description           |
|--------|---------------------------------------|-----------------------|
| GET    | `/api/v1/shipments`                   | List (paginated)      |
| GET    | `/api/v1/shipments/{id}`              | Get by ID             |
| POST   | `/api/v1/shipments`                   | Create shipment       |
| PUT    | `/api/v1/shipments/{id}`              | Update shipment       |
| DELETE | `/api/v1/shipments/{id}`              | Delete shipment       |
| POST   | `/api/v1/shipments/{id}/events`       | Register event        |
| GET    | `/api/v1/shipments/{id}/events`       | Event history         |
| GET    | `/api/v1/shipments/{id}/eta`          | Calculated ETA        |

### Voyages

| Method | Endpoint                              | Description           |
|--------|---------------------------------------|-----------------------|
| GET    | `/api/v1/voyages`                     | List voyages          |
| GET    | `/api/v1/voyages/{id}`                | Get voyage details    |
| POST   | `/api/v1/voyages`                     | Create voyage         |
| GET    | `/api/v1/voyages/{id}/schedule`       | Full port rotation    |

### Vessels / Ports / Alerts / Analytics / Billing

See full documentation at `/swagger-ui.html` after starting the application.

## Project Structure

```
src/main/java/com/freightflow/
├── config/             # Security, OpenAPI, RabbitMQ, Redis configs
├── modules/
│   ├── shipment/       # Shipment CRUD, tracking, events
│   ├── voyage/         # Voyage management
│   ├── vessel/         # Vessel registry
│   ├── port/           # Port data (UNLOCODE)
│   ├── event/          # Tracking events
│   ├── alert/          # Proactive alerts
│   ├── auth/           # Authentication & API keys
│   ├── webhook/        # Webhook subscriptions
│   ├── analytics/      # Operational KPIs
│   └── billing/        # Stripe integration
├── shared/
│   ├── exception/      # Global error handling (RFC 7807)
│   ├── security/       # JWT provider, filters
│   ├── pagination/     # Page request/response
│   └── util/           # ETA calculator, date utils
└── infrastructure/
    ├── messaging/      # RabbitMQ event publisher
    └── external/       # Stripe service
```

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

## Author

**Kauê** — Logistics professional with 15+ years of experience in multimodal port/rail operations at Mercosul Line (CMA CGM Group), Santos/SP, Brazil. Transitioning into software development with deep domain expertise in maritime freight.
