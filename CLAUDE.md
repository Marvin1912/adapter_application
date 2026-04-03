# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build the entire project
./gradlew build

# Build a specific module
./gradlew :plants:build

# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :plants:test

# Run a single test class
./gradlew :plants:test --tests PlantControllerTest

# Run a single test method
./gradlew :plants:test --tests PlantControllerTest.testMethod

# Checkstyle (enforced, zero warnings allowed)
./gradlew checkstyleMain checkstyleTest
./gradlew checkstyleAll    # all subprojects at once

# Build the Spring Boot JAR (boot module only)
./gradlew :boot:bootJar

# Build Docker image
./gradlew buildAdapterDockerImage

# Local PostgreSQL (costs-db on port 5432, user=marvin, password=password, db=costs)
docker compose -f docker-compose-local.yaml up -d
```

## Architecture

This is a **multi-module Gradle monolith** using Spring Boot with an orthogonal architecture. All modules are packaged into a single Spring Boot JAR (`backend.jar`) via the `boot` module.

### Module Layers

- **boot** - Application entry point (`com.marvin.Application`). Aggregates all modules, enables scheduling. Only module that produces a bootJar.
- **common** - Shared utilities (JacksonMapper, DTOs, NullSafeUtil). Every other module depends on it automatically (configured in root `build.gradle`).
- **entities** - JPA entity definitions. All entities extend `BasicEntity` which provides `creationDate` and `lastModified` fields.
- **database** - Repositories and Flyway migrations. Uses two Flyway instances: `flywayMain` (schema: `finance`, migrations: `db/migration/costs`) and `flywayExports` (schema: `exports`, migrations: `db/migration/exports`).
- **api** - REST API facade and orchestration layer. Depends on importer, exporter, uploader, camt, database, entities.

### Feature Modules

Self-contained vertical slices: plants, it-news, vocabulary, mental-arithmetic, image-server. Each feature module manages its own Flyway migrations in separate schemas and migration paths.

### Key Technical Choices

- **Reactive stack**: Spring WebFlux (not MVC) — controllers return `Mono<>` / `Flux<>`
- **Mapping**: MapStruct for entity-DTO conversion (`@Mapper(componentModel = "spring")`)
- **Metrics**: Micrometer/Prometheus gauges (e.g., plant watering/fertilizing status)
- **API docs**: Springdoc OpenAPI with WebFlux UI
- **`-parameters` compiler flag** is enabled (preserves parameter names at runtime)

## Code Style & Testing

- Checkstyle is enforced with zero warnings (`config/checkstyle/checkstyle.xml`)
- 120 char line length, 4-space indentation, no tabs, no wildcard imports
- Max 50 lines per method, max 7 parameters
- K&R brace style
- JUnit 5 + Mockito + `reactor-test` (StepVerifier for reactive assertions)
- Tests use `@ExtendWith(MockitoExtension.class)` for unit tests
