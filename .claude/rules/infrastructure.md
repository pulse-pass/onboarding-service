---
description: Local infrastructure, Docker and scripts conventions
paths:
  - "infrastructure/**"
  - "Dockerfile"
  - "README.md"
  - "src/main/resources/application*.properties"
---

# Infrastructure

- **ONB-018** `infrastructure/` holds `docker-compose.yml`, `.env.example`, `database.sql`, `service-start.sh` and `service-stop.sh`, plus the external `pulse-pass` network.
- **ONB-019** The compose declares `name: <repo>-test` and gives every variable the scripts export (`SERVICE_PREFIX`, `MYSQL_HOST`, `PUBLISHED_API_PORT`, each `<X>_URL`) a `test` mode default, e.g. `${SERVICE_PREFIX:-<repo>-test}`.
- **ONB-020** Because of those defaults, `docker compose` without the scripts resolves to test mode (the CI log step relies on it); the scripts still pass `-p` and export those variables for the chosen mode.
- **ONB-021** The service keys of the compose are generic (`api`, `mysql`, `mockserver`) and the real name comes from `container_name` with `SERVICE_PREFIX`, because the key becomes a DNS alias on the `pulse-pass` network and both modes run together.
- **ONB-022** Always address another container by its `container_name`, never by the compose key.
- **ONB-023** `test` mode (`./infrastructure/service-start.sh test`, default, used by the CI) is self-sufficient: project `<repo>-test`, containers `<repo>-test-api`, `<repo>-test-mysql` and `<repo>-test-mockserver`; it never depends on `pulse-pass-infrastructure`.
- **ONB-024** `test` mode has its own MySQL (profile `test`, `database.sql` mounted in `initdb`) and MockServer, and the API on `API_PORT` (8081).
- **ONB-025** The MySQL healthcheck pings over TCP (`mysqladmin ping -h 127.0.0.1`) so it only passes after `database.sql` has run.
- **ONB-026** `development` mode starts only the API: project `<repo>`, container `<repo>-api` (the name other services use in `<NAME>_DEVELOPMENT_URL`).
- **ONB-027** The API is published on `DEVELOPMENT_API_PORT` (8091), using the shared `pulse-pass-mysql` (host `pulse-pass-mysql`, port 3306) from `pulse-pass-infrastructure`.
- **ONB-028** Before anything else (before calling `service-stop.sh`, so a missing infrastructure never takes down a running API), the script requires `pulse-pass-mysql` to be `healthy`, otherwise it exits asking to start the shared infrastructure.
- **ONB-029** As root (`MYSQL_ROOT_PASSWORD`, `root` like the infrastructure), the script creates the `.env` user with `CREATE USER IF NOT EXISTS` and a `GRANT ALL PRIVILEGES` only on the service schema, then applies `database.sql`.
- **ONB-030** The `.env` user, password and schema must not contain quotes or backslashes, because the script writes them into SQL.
- **ONB-031** Table changes only reach the shared MySQL when the script runs again; it is idempotent and never recreates what exists.
- **ONB-032** Both modes can run at the same time (distinct projects, container names and ports).
- **ONB-033** `service-stop.sh [test|development]` (default `test`) tears down only that mode's project (`down -v --remove-orphans --rmi local`) and never touches `pulse-pass-mysql` or the schema; `service-start.sh <mode>` calls it for the same mode first.
- **ONB-034** Redis and Kafka are added to the compose (test mode, own containers) and to `.env.example` (development uses `pulse-pass-redis` and `pulse-pass-kafka`) only in the task that makes the service use them.
- **ONB-035** The ports 3306, 6379, 9094 and 8180 belong to the shared infrastructure and the service must not publish them.
- **ONB-036** The mode comes from the argument of the scripts, never from editing `.env`.
- **ONB-037** Each external dependency has a `<X>_TEST_URL` / `<X>_DEVELOPMENT_URL` pair in `.env`, and the script exports `<X>_URL`, which feeds the gateway property `app.<name>.url`; the test URLs point to the `<repo>-test-mockserver` container.
- **ONB-038** The start script creates the `pulse-pass` network if missing and copies `.env.example` on first run.
- **ONB-039** Redis, Kafka, Kafka UI, Keycloak, Mailpit and MinIO live in the `pulse-pass-infrastructure` repository.
- **ONB-040** README: running the service only needs Docker (the image builds with its own JDK); Java 21 is only needed to run `./mvnw` (lint and tests) outside the container.
- **ONB-041** No comments in `infrastructure/`, `Dockerfile` or `application.properties`: names and structure must explain them, and decisions go to the rules or the README.
- **ONB-042** Generated or third-party files are kept as they come, comments included (`mvnw`, `checkstyle.xml`).
- **ONB-043** Secrets in `.env.example` are placeholders or fixed test values (`onboarding-service-development-secret`); the real value goes only to the git-ignored `.env`.
- **ONB-044** A provider URL never includes the version or realm path, the gateway adds it (`KEYCLOAK_DEVELOPMENT_URL` is `http://pulse-pass-keycloak:8080`, the gateway calls `/realms/{realm}/...`).
- **ONB-045** A secret in `.env` that may contain `$`, such as the Keycloak client secret, goes in single quotes (`KEYCLOAK_CLIENT_SECRET='...'`).
- **ONB-046** Quotes are needed because `service-start.sh` sources the file and Docker Compose interpolates it, and without quotes the value silently becomes empty.
- **ONB-047** `MockServerHealthcheck.java` is part of `infrastructure/`: the MockServer image has no shell or curl, so its healthcheck runs this file with `java`.
