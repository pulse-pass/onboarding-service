---
description: Routes, JSON, schemas, responses and internal routes
paths:
  - "**/api/**"
  - "**/application/**"
---

# API

- **ONB-073** Dates and times in responses are `Instant`, serialized as ISO 8601 in UTC with `Z` (`"created_at": "2026-11-03T22:50:27.442Z"`), never `LocalDateTime`, which goes out without a zone and is read as local time by the clients.
- **ONB-074** Entities keep `LocalDateTime` in UTC (the containers and MySQL run in UTC), and the response converts it in `from*` with `toInstant(ZoneOffset.UTC)`.
- **ONB-075** UUID fields are validated with `@UUID(allowNil = false, letterCase = UUID.LetterCase.INSENSITIVE)`.
- **ONB-076** No nested records in schemas, DTOs or responses: each nested object is its own file.
- **ONB-077** Nested input objects live in `definitions/` (`api/schemas/definitions`, `application/dtos/input/definitions`) with generic names, without the operation (e.g. `PersonPhoneNumberSchema`, `PersonPhoneNumberDTO`); create a specific variant only when the validations diverge.
- **ONB-078** Schemas validate only the shape of the request (required fields, size, digits-only format, list size).
- **ONB-079** Business rules (document check digits, allowed values such as the collaborator role) live in the service and throw domain exceptions; no custom Bean Validation annotations.
- **ONB-080** Route paths start with the service name (`/onboarding/legal_persons`), in snake_case with underscores, never hyphens.
- **ONB-081** Service-to-service routes live under `/<service>/internal/**` (e.g. `/organization/internal/organizations`).
- **ONB-082** The gateways expose routes without the service prefix and add it when forwarding.
- **ONB-083** The `backoffice-gateway` also exposes some internal routes, without the service prefix as `/internal/**` (e.g. `/internal/organizations`), only to the admin role; no other gateway exposes them.
- **ONB-084** JSON in snake_case.
- **ONB-085** Accepted values that are a subset of a domain enum use an enum of their own in the schema layer (`<Action><Resource><Field>Schema` in `api/schemas/definitions`), never `@Pattern`; `toDTO()` converts it with `name()`.
- **ONB-087** JSON fields (request, query and response) and parameters follow the name of the database column or table, never abbreviated (`organization_status`, `organization_rejection_reason`, never `status` or `rejection_reason`).
- **ONB-088** In Java, the same full name in camelCase (`organizationStatus`).
- **ONB-089** Internal route (`/<service>/internal/**`): the controller takes `@RequestHeader(value = "internal-token", required = false)` and `@RequestHeader(value = "selected-user-roles", required = false)`.
- **ONB-090** It calls `internalRequestValidator.validateInternalTokenOrAdmin(internalToken, selectedUserRoles)` on its first line (no filter).
- **ONB-091** The call is valid when the token equals the configured one (shared by every application, `INTERNAL_TOKEN`, compared with `equals`) or when `selected-user-roles` (comma-separated, each item trimmed) contains `admin`; otherwise `UnauthorizedInternalRequestException` (401 `GEN00401`).
- **ONB-092** There is no token-only validator.
- **ONB-093** Service-to-service calls use the token; the admin reaches the route through the `backoffice-gateway`, which sends `selected-user-roles`.
- **ONB-064** The `selected-agent-key` of a list route resolved by the token is read with `required = false` and validated by `InternalRequestValidator` (`validateSelectedAgentKey`; absent or blank → 401 `GEN00401`, `MissingSelectedAgentKeyException`), never with Bean Validation or a required header.
- **ONB-094** A create endpoint returns only the created resource (no child lists).
- **ONB-095** Top-level responses in `api/responses/` are named `<Action><Resource>Response` (`ListOrganizationsResponse`, `CreateLegalPersonResponse`); nested objects in `definitions/` take only the resource name (`LegalPersonOwnerResponse`).
- **ONB-096** Responses of internal routes are named `<Action>Internal<Resource>Response` (`CreateInternalOrganizationResponse`) and their output DTOs `<Action>Internal<Resource>DTO` (`GetInternalLegalPersonDTO`).
- **ONB-097** Reusable nested responses live in `api/responses/definitions/` as `<Entity>Response` with `fromEntity`.
- **ONB-098** Pagination lives in `api/responses/pagination/`.
- **ONB-099** Input DTOs (`application/dtos/input`) exist only for fields that come from the request body through a schema; header, path and query values are passed to the service as plain parameters (e.g. `getSession(String personKey, String legalPersonKey, String roles)`).
- **ONB-100** The `OutputDTO` suffix (and `from*Output` in responses) is only for what an external gateway returns.
- **ONB-101** Service outputs that do not come from a gateway are `<Action><Resource>DTO` in `application/dtos/output`, named after the response they build with the suffix swapped (`GetLegalPersonDTO` → `GetLegalPersonResponse`), and the response uses `fromDTO`.
- **ONB-102** An option backed by a closed list that only has to be sent back to a provider, such as the company type, is a typed enum in the schema instead of a service rule.
- **ONB-103** `GET /onboarding/auth/me` answers who is logged in from headers the backoffice gateway takes from the already validated token: `selected-user-key` (the `person_key` of the person, required).
- **ONB-104** `selected-agent-key` is the `legal_person_key` of the organizer's company (optional: absent for the admin) and `selected-user-roles` is the realm roles separated by commas (required, split without filtering).
- **ONB-105** The service returns `GetSessionDTO` and the response is `GetSessionResponse` (`fromDTO`) `{person_key, name, document_number, roles, legal_person}`.
- **ONB-106** `legal_person` (`SessionLegalPersonResponse`) is `{legal_person_key, name}` or `null` (still present in the JSON) when there is no agent key.
- **ONB-107** A person that does not exist, an agent key that matches no company, or a person who is not a collaborator of that company throws `InvalidSessionException` → 401 `GEN00401` (`error.invalid_session`, generic authentication error like the others).
- **ONB-108** Those three checks run in this order, regardless of the collaborator status (`LegalPersonCollaboratorRepository.existsByLegalPersonKeyAndNaturalPersonKey`), each failure with a `warn` log carrying `person_key` and `legal_person_key`.
- **ONB-109** `findByPersonKey` on `LegalPersonRepository` receives the `person_key` of the legal person, which is the `legal_person_key`.
- **ONB-110** Authentication routes (`/onboarding/auth/login`, `/refresh`, `/logout`) only relay the identity provider session: the tokens issued by Keycloak go back untouched in `CreateSessionResponse`.
- **ONB-111** `invalid_grant` becomes 401 `GEN00401` (`InvalidCredentialsException` on login, `InvalidRefreshTokenException` on refresh and logout).
