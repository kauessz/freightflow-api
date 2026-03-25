# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Initial project scaffolding with Spring Boot 3.3.x + Java 21
- Maven build configuration with all dependencies
- Docker Compose setup (PostgreSQL 16, Redis 7, RabbitMQ 3.13)
- Multi-stage Dockerfile for optimized production images
- GitHub Actions CI pipeline (test → build → docker)
- Application profiles: dev, test, prod
- README with project documentation and API endpoint reference
