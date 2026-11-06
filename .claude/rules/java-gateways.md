---
description: External gateways: structure, RestClient, DTOs, failures and secrets
paths:
  - "**/domain/gateways/**"
  - "**/infrastructure/gateways/**"
  - "**/application/**"
  - "**/domain/exceptions/**"
  - "src/main/resources/application*.properties"
---

# Gateways

## Structure and naming

- **ONB-150** Nothing outside `infrastructure/` carries the name of an external provider, and domain exceptions and message keys describe the capability (`UserCreationException`, `OrganizationCreationException`), never who implements it.
- **ONB-151** One gateway per external system, not per resource: all calls to a system go through the same class.
- **ONB-152** The domain interface is named after the role or the internal service (`IdentityProviderGateway`, `OrganizationGateway`).
- **ONB-153** Each gateway has its own folder in `domain/gateways/<gateway>/` (named after the gateway, not the provider), holding the interface and a `dtos/` folder (e.g. `identityprovider/IdentityProviderGateway`, `identityprovider/dtos/CreateUserInputDTO`).
- **ONB-154** The interface receives and returns those DTOs, named after the operation, never loose parameters.
- **ONB-155** The implementation lives in `infrastructure/gateways/<provider>/`, carries the name of the system, one class per system (e.g. `keycloak/KeycloakGateway`, `organizationservice/OrganizationServiceGateway`), and uses `RestClient`.
- **ONB-156** Request and response bodies are typed records in `infrastructure/gateways/<provider>/dtos` (`<Provider><Operation>Request` / `Response`) with `@JsonProperty` on every field; no `Map` bodies.

## Configuration

- **ONB-157** A service that calls another through a gateway has the `spring-boot-starter-restclient` dependency (Spring Boot 4 does not auto-configure `RestClient.Builder` without it).
- **ONB-158** Timeouts come from `spring.http.clients.connect-timeout` and `spring.http.clients.read-timeout` in `application.properties`, which Spring Boot applies to the auto-configured `RestClient.Builder`.
- **ONB-159** Gateways receive the builder injected and never build a request factory by hand.

## Calls and failures

- **ONB-161** Each public operation of a gateway makes its own main HTTP call inline (request, `try`, status and body checks, mapping of the response), even when that duplicates code across operations.
- **ONB-162** Private methods are only for auxiliary steps that are not the operation itself (a token or role lookup, small predicates such as `isInvalidGrant`).
- **ONB-163** The checks of a response body shared by several operations may be a private `validate*` method that only validates and returns nothing; the mapping to the output DTO stays inline in each operation. This keeps the new-code duplication below the 3% of the SonarQube quality gate.
- **ONB-164** A gateway checks the transport failure, the response status and an empty body, and nothing else: it never validates the fields of a response one by one (`body.id() == null`), and it trusts the contract of the system it calls.
- **ONB-165** A transport failure or unexpected status in the operation itself is logged in the gateway and becomes a domain exception for the operation, mapped to 422 (`HttpStatus.UNPROCESSABLE_CONTENT`, not the deprecated `UNPROCESSABLE_ENTITY`) in the `STATUS_MAP`, never to a 5xx.
- **ONB-167** Each `try` wraps only the HTTP call (one `try` per call); status and body checks come after it.
- **ONB-168** Every gateway call in a service is inside a `try` with a `catch (Exception ex)`: any failure of the call is logged and becomes the domain exception of the operation, so it never leaves as a 500.
- **ONB-169** A domain exception with a different meaning for the client (e.g. `InvalidCredentialsException`, `InvalidRefreshTokenException`) gets its own `catch` before it and is rethrown.
- **ONB-170** The access token is part of the operation: its failure (transport, error status or no token in the body) is logged and becomes the same domain exception (e.g. `UserCreationException`).
- **ONB-171** Other auxiliary calls (lookups such as the role) run before the operation, outside its `try`, and fail with a generic exception (`IllegalStateException` or the `RestClientException` itself) → 500 `GEN00500`.
- **ONB-172** Reading the error status of the token endpoint uses `onStatus` with an empty handler so the status and body checks come after the single `try` around the call.

## Token renewal

- **ONB-112** The realm revokes the refresh token on every renewal (`revokeRefreshToken`, `refreshTokenMaxReuse` 0): each refresh returns a new refresh token.
- **ONB-113** A used refresh token is refused with `invalid_grant` and Keycloak then ends the whole session, so the new token of that renewal is refused too.
- **ONB-114** The service just relays this; it never caches or retries a refresh token.
