## Token Discipline

- Prefer the shortest correct answer.
- Skip preambles, recap, and praise.
- Ask at most one blocking question; otherwise make a reasonable assumption and proceed.
- Use prose by default. Use bullets only for distinct items.
- Quote or paste only when exact text is required.
- Summarize command output instead of reproducing it.
- Avoid repeating requirements, file lists, or prior context.
- For code changes, report only outcome, key files, and any risk or missing verification.

## Java Standards

- Treat this as a Java 21 Maven Spring Boot codebase.
- Target a monolithic Spring Boot MVC design, not microservices.
- Prefer the package root `com.utilitybilling` for new application code.
- Keep source under `src/main/java` and tests under `src/test/java`.
- Follow feature-based packages where practical: `auth`, `user`, `customer`, `meter`, `reading`, `tariff`, `billing`, `payment`, `notification`, `common`.
- Inside each feature, keep normal Spring layers: `controller`, `service`, `repository`, `entity`, `dto` when needed.
- Prefer constructor injection. Avoid field injection.
- Keep controllers thin; move business rules into services.
- Do not expose entities directly from controllers; use request/response DTOs.
- Keep persistence concerns in repositories/entities; avoid leaking entities directly across API boundaries.
- Prefer explicit validation, error handling, and HTTP status mapping for public endpoints.
- Centralize error handling in a global exception handler and use a consistent API response wrapper.
- Add or update tests with behavior changes. Prefer focused slice or unit tests when full `@SpringBootTest` is unnecessary.
- Use clear class and method names, small methods, and minimal comments.
- Do not introduce new frameworks or major patterns unless the codebase needs them.
- Validate Java changes with Maven when feasible.

## Project Architecture

- Build around this lifecycle: customer -> meter -> reading -> bill -> payment -> notification.
- Keep the main business logic in services, not controllers or repositories.
- Model the system around these core relationships:
  - customer owns meters
  - meter has readings
  - reading generates a bill
  - bill receives payments
  - important bill/payment events create notifications
- Preserve the monolith shape even if features are added; prefer modules/packages over architectural expansion.

## Domain Rules

- Use PostgreSQL as the primary database target.
- Use `BigDecimal` for money and billing arithmetic; avoid `double`/`Double` for monetary fields and calculations.
- Enforce important invariants in both Java validation and database constraints where applicable.
- Keep tariff versions append-only. Prefer new tariff records over destructive updates.
- Apply tariffs by meter type and effective date for the billing cycle being generated.
- Enforce one meter reading per meter per billing month/year.
- Enforce unique customer national ID, unique meter number, unique bill reference, and unique user email.
- Only active customers/meters should participate in billing flows unless a requirement explicitly says otherwise.
- Support partial payments and full payments; update outstanding balance and bill status atomically.

## Security

- Use Spring Security with JWT, BCrypt password hashing, stateless sessions, and role-based authorization.
- Treat `/api/auth/**`, Swagger UI, and OpenAPI docs as public unless requirements change.
- Prefer method-level authorization with `@PreAuthorize` for business actions.
- Respect these roles: `ROLE_ADMIN`, `ROLE_OPERATOR`, `ROLE_FINANCE`, `ROLE_CUSTOMER`.

## API and Persistence

- Prefer REST endpoints under `/api/...` and keep naming aligned with the project context.
- Use Swagger/OpenAPI for endpoint visibility when adding or changing APIs.
- Keep database-triggered notifications in mind when implementing bill creation and full-payment completion.
- If schema or trigger scripts are added, keep them explicit and PostgreSQL-compatible.

## Skill Use

- Use `token-minimizer` when the user asks for minimal token usage, terse output, compact instructions, or low-context operation.
