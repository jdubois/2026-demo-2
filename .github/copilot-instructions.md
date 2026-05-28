# Copilot instructions for ticket-manager

## Build, test, and lint commands

- Full verification: `./mvnw verify`
  - Runs the frontend Maven plugin first (`npm install`, `npm run build`), then compiles, tests, and packages the Spring Boot app.
  - Requires Docker because backend tests use Testcontainers PostgreSQL.
- Backend tests: `./mvnw test`
- Single backend test class: `./mvnw -Dtest=TicketManagerApplicationTests test`
- Single backend test method: `./mvnw -Dtest=TicketManagerApplicationTests#seedsInitialGitHubTickets test`
- Run the application: `./mvnw spring-boot:run`
  - Spring Boot Docker Compose support starts PostgreSQL from `compose.yaml`.
- Frontend dev server: `cd frontend && npm run dev`
  - Vite runs on `VITE_PORT` or `5173` and proxies `/api` to Spring Boot on `SPRING_BOOT_PORT` or `8080`.
- Frontend build: `cd frontend && npm run build`
- Frontend lint: `cd frontend && npm run lint`
- Frontend format: `cd frontend && npm run format`
- Frontend unit tests: `cd frontend && npm run test:unit -- --run`
- Single frontend test file: `cd frontend && npm run test:unit -- --run src/components/__tests__/TicketStatusBadge.spec.js`
- Optional native/container commands from `HELP.md`:
  - `./mvnw spring-boot:build-image -Pnative`
  - `./mvnw native:compile -Pnative`
  - `./mvnw test -PnativeTest`

## High-level architecture

- This is a Spring Boot 4 / Java 25 backend with a Vue 3 + Vite frontend in `frontend/`.
- The backend package root is `com.example.ticketmanager`.
  - `domain/Ticket.java` is both the JPA entity and response model. It owns validation constraints, `createdAt`/`updatedAt` lifecycle callbacks, a unique GitHub issue link, and string-backed `TicketStatus`.
  - `repository/TicketRepository.java` is the only persistence abstraction and extends `JpaRepository`.
  - `web/TicketController.java` exposes `/api/tickets` CRUD endpoints directly against the repository; there is intentionally no service layer for this simple CRUD flow.
  - `web/TicketRequest.java` is the validated request DTO used for create/update.
  - `config/TicketDataInitializer.java` seeds the 10 GitHub tickets only when the ticket table is empty.
- PostgreSQL is the database.
  - Local runtime uses Spring Boot Docker Compose support with `compose.yaml`.
  - Tests use `TestcontainersConfiguration` with `@ServiceConnection`.
  - `application.properties` imports optional `.env` values and uses `spring.jpa.hibernate.ddl-auto=update`.
- The frontend data flow is:
  - `src/services/tickets.js` wraps `fetch` calls to `/api/tickets`.
  - `src/stores/tickets.js` is the Pinia store for tickets, loading/saving state, status metadata, repository lists, and status counts.
  - `src/views/HomeView.vue` contains the dashboard, filters, add/edit form, and ticket cards.
  - `src/components/TicketStatusBadge.vue` centralizes status badge rendering.
- Vite builds the SPA into `src/main/resources/static`; Spring Boot serves that output in packaged/runtime mode.

## Key conventions

- Prefer Java LSP for Java navigation and refactors; `.github/lsp.json` configures JDTLS for `.java` files.
- Keep API routes under `/api`. Do not add controller-level CORS for local development; Vite proxying handles `/api` in dev mode.
- Keep ticket validation aligned across `Ticket`, `TicketRequest`, and the Vue form:
  - repository format is `owner/repository`
  - link format is `https://github.com/{owner}/{repository}/issues/{number}`
- Persist `TicketStatus` as strings with `@Enumerated(EnumType.STRING)`. When adding or renaming statuses, update both `TicketStatus.java` and `frontend/src/stores/tickets.js`.
- Preserve idempotent seed behavior: seed data should not overwrite or recreate user-managed tickets after the first database initialization.
- Use `ResponseStatusException` for explicit REST errors such as duplicate links (`409`) and missing tickets (`404`).
- Java code uses constructor injection for required collaborators. Configuration/controller classes that are only used inside their package are package-private.
- Frontend shared ticket state belongs in the Pinia store; keep HTTP details in `src/services/tickets.js` rather than calling `fetch` directly from Vue views.
- `vite.config.js` exports a callback config, so `vitest.config.js` must merge the resolved object via `viteConfig({ mode: 'test', command: 'serve' })`.
- Formatting follows `.editorconfig`: 4 spaces for Java/XML/properties, 2 spaces for JavaScript/Vue/JSON/YAML.
- Use `application.properties` for Spring configuration. The app imports `.env`; do not read or print `.env`, use `.env.sample` for placeholders.
