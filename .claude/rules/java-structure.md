---
description: Stack, packages, shared code, entities and common/
paths:
  - "src/main/java/**/*.java"
---

# Java structure

- **ONB-203** Java 21, Spring Boot 4.x, Maven (no Gradle, no Lombok).
- **ONB-204** Package `com.pulsepass.<service>`.
- **ONB-205** Packages: `api` (controllers, schemas, responses, exceptions, validators), `application` (services, dtos/input), `domain` (entities, repositories, exceptions, `UnitOfWork`), `infrastructure/jpa` (JPA entities, `Jpa*Repository`, `*RepositoryImpl`, `JpaUnitOfWork`) and `common`.
- **ONB-206** Code shared by every service is identical in all of them except for the package, tests included.
- **ONB-207** The shared code is `InternalRequestValidator` (`hasAdminRole`, `validateInternalTokenOrAdmin`, `validateSelectedAgentKey`) and its test, `DomainException`, the generic exceptions (`UnauthorizedInternalRequestException`, `MissingSelectedAgentKeyException`), `ErrorResponse`, `ExtraFieldErrorResponse`, `PaginatedResponse`/`PaginationResponse`, `UnitOfWork`/`JpaUnitOfWork` and `JsonHandlers`.
- **ONB-208** A change to one of them (a rename, a new case, a new method) is applied to every repository that has it in the same task; before closing a task, diff these files across the repositories.
- **ONB-209** Pure domain entity + JPA `*Entity` with `fromDomain`/`toDomain`; repository interface in the domain, implementation in `infrastructure/jpa`.
- **ONB-210** JPA `*Entity` classes have only fields, constructors and `fromDomain`/`toDomain` — no getters/setters (Hibernate uses field access); this also keeps them from being flagged as duplicates of the domain entities.
- **ONB-211** Inside entities (domain and JPA), always reference the object's own fields with `this.` (getters, `toDomain`, `toHistoryData`).
- **ONB-212** Never read another entity's fields directly (e.g. `history.organization.name`); go through a method such as `toDomain()`, because lazy proxies only load on method calls and their fields stay empty.
- **ONB-213** Logic that builds data from the entity itself (e.g. the history snapshot, `toHistoryData()`) belongs to the domain, not to the service.
- **ONB-214** `common/` holds only general-purpose language helpers (string, JSON, document math), named `<Subject>Handlers` (`JsonHandlers`, `DocumentHandlers`) and placed directly in `common/`, without a `utils` subfolder.
- **ONB-215** Framework concerns (HTTP clients, timeouts, Spring beans) never go to `common/`: prefer the Spring Boot configuration (properties and auto-configured beans such as `RestClient.Builder`); what is left stays in the class that uses it.
