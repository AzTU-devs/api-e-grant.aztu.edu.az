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

The integration tests cover the full critical path — signup → OTP → signin → profile
completion, then lookups → project → team (limit) → budget (cap) → submit (lock gate) →
expert assignment → report → admin approval → public views.

On macOS without Docker Desktop you can use [Colima](https://github.com/abiosoft/colima):

```bash
colima start --cpu 4 --memory 6
export DOCKER_HOST="unix://${HOME}/.colima/default/docker.sock"
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE="/var/run/docker.sock"
./mvnw verify
```

## Environment variables

See [`.env.example`](.env.example). Key vars: `DATABASE_URL`, `DATABASE_USERNAME`,
`DATABASE_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, `SMTP_*`, `MAIL_ENABLED`,
`STORAGE_BASE_DIR`, `SPRING_PROFILES_ACTIVE` (`local` | `prod`). No secrets are committed.

## API

REST surface under `/api/v1`, documented in OpenAPI (Swagger UI at `/swagger-ui.html`).
Errors are RFC 7807 `application/problem+json`. Auth is stateless JWT (bearer); public
(`/api/v1/public/**`), auth (`/api/v1/auth/**`) and lookup `GET`s are unauthenticated.
The full legacy→new endpoint mapping is in [`API_MAP.md`](API_MAP.md).

## PDF fonts

The PDF export embeds `/fonts/NotoSans-Regular.ttf` when present on the classpath
(`src/main/resources/fonts/`), which is recommended for correct Azerbaijani glyphs
(ə, ğ, ı, ö, ü, ç, ş). Without it the export still works using default fonts.

## Build status

Complete. All twelve modules are implemented; `./mvnw verify` is green (unit +
Testcontainers integration covering the critical flows + Spring Modulith boundary
verification). The Flyway schema matches [`docs/DB_ARCHITECTURE.md`](docs/DB_ARCHITECTURE.md)
(16 tables + the `v_budget_totals` view + the `system_lock` gate). Every legacy capability
is reachable in the new `/api/v1` API — see [`API_MAP.md`](API_MAP.md).
