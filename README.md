# AZTU E-Grant — Backend (Spring Boot modular monolith)

University grant-competition platform. Researchers submit grant **projects**, build a
**team**, attach a budget estimate (**smeta**), plan monthly **activities**, and file
**quarterly reports**; **experts** review and **assess**; admins approve users/projects
and open/lock the submission window.

This is a from-zero rebuild of a legacy Flask app as a clean Spring Boot **modular
monolith**. Schema source of truth: [`docs/DB_ARCHITECTURE.md`](docs/DB_ARCHITECTURE.md).
Behavioural source of truth: the legacy Flask app. Capability mapping:
[`API_MAP.md`](API_MAP.md). Requirements & rules: [`REQUIREMENTS.md`](REQUIREMENTS.md).

## Tech stack

- Java 21, Maven (wrapper committed), Spring Boot 3.5.x
- Spring Modulith (enforced module boundaries + application events)
- Spring Web, Spring Data JPA / Hibernate, PostgreSQL
- Flyway (the app owns the schema; `ddl-auto=validate`)
- Spring Security + JWT (jjwt), BCrypt
- Jakarta Bean Validation, MapStruct (records for DTOs)
- springdoc-openapi (Swagger UI), Spring Mail + Thymeleaf
- openhtmltopdf (PDF) + Apache POI (Excel)
- Bucket4j rate limiting
- Testing: JUnit 5, Spring Boot Test, Testcontainers (Postgres), Spring Modulith test

## Modules (bounded contexts)

`shared` (kernel), `iam`, `institution`, `priority`, `project`, `budget`, `expert`,
`report`, `admin` (system lock), `publicapi`, `notification`, `document`.

Cross-module access goes only through a module's `api` package or via Modulith
application events — never into another module's `internal` package. Enforced by
`ModularityTests` (`ApplicationModules.verify()`).

Each module is layered: `api` (DTOs, exposed services, events) · `web` (controllers) ·
`domain` (entities, enums) · `internal` (repositories, service impls, mappers).

## Run

### With Docker (app + Postgres)

```bash
cp .env.example .env       # adjust secrets
docker compose up --build
```

App: http://localhost:8080 · Swagger UI: http://localhost:8080/swagger-ui.html

### Locally (needs JDK 21 + a Postgres)

```bash
docker compose up -d postgres          # or point DATABASE_URL at your own
./mvnw spring-boot:run                  # uses the 'local' profile by default
```

## Test

```bash
./mvnw verify
```

Runs unit tests, Testcontainers integration tests (spins up Postgres so Flyway,
generated columns, enums and the `v_budget_totals` view run against real Postgres), and
the Spring Modulith boundary verification. **Requires Docker** for Testcontainers.

## Environment variables

See [`.env.example`](.env.example). Key vars: `DATABASE_URL`, `DATABASE_USERNAME`,
`DATABASE_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, `SMTP_*`, `MAIL_ENABLED`,
`STORAGE_BASE_DIR`, `SPRING_PROFILES_ACTIVE` (`local` | `prod`). No secrets are committed.

## Build status

Foundation (this iteration): scaffold + `shared` + `iam` + complete Flyway schema +
Modulith boundary test + Testcontainers harness. Remaining modules are tracked in
`API_MAP.md` (🚧) and land in follow-up iterations.

> Note: this environment has no local JDK/Docker, so `./mvnw verify` must be run on a
> machine with JDK 21 + Docker installed.
