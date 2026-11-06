---
description: Unit and integration testing strategy and conventions
paths:
  - "src/test/**"
---

# Tests

## Unit tests

- **ONB-240** Unit tests (JUnit 5 + Mockito + AssertJ) in `src/test/java/.../unit/`, mirroring the main packages: one test class per production class, never grouping several classes in one test.
- **ONB-241** Test names `method_scenario_expectedResult`.
- **ONB-242** External gateways (`infrastructure/gateways`) are unit-tested against `StubHttpServer`, a local HTTP server in the test package that records what it receives.
- **ONB-243** Unit tests may keep constants, `@BeforeEach` setup and private helpers.

## Integration tests

- **ONB-244** Black-box integration tests with REST Assured against the service running in Docker Compose (no Testcontainers), in `src/test/java/.../integration/`: `BaseIntegrationTest` (URL, port and token from environment variables) and one class per route.
- **ONB-245** Classes are `Test<Method><Action>` for routes that change something (e.g. `TestPostCreateLegalPerson`) and `Test<Method><Resource>` for GET routes, with no `Read` (e.g. `TestGetSession`).
- **ONB-246** They run with `./mvnw test -Pintegration`; `./mvnw test` runs only the unit tests.
- **ONB-247** REST Assured must be 6.x: Spring Boot 4 brings Groovy 5 and Jackson 3, which REST Assured 5.x cannot use.
- **ONB-248** Integration tests are always run against the real compose (`./infrastructure/service-start.sh test` + `./mvnw test -Pintegration`) before a task is closed; unit tests with mocks do not catch persistence problems (lazy loading, constraints, schema validation).
- **ONB-249** Integration tests only go through the HTTP API: never open a database connection (JDBC, `DriverManager`, SQL strings) or touch the schema to set up state.
- **ONB-250** A state that no route can produce yet (e.g. a rejected organization before the rejection route exists) is covered by unit tests, and its integration test comes with the route that produces it.
- **ONB-251** Integration test classes have no constants other than the URLs of the routes the class calls directly: the tested route and any other route used to check the result (e.g. `private static final String POST_ORGANIZATION = "/organization/internal/organizations";`).
- **ONB-252** A prerequisite created through another route (e.g. approving an organization before deleting it) goes through `SetupTools`, which keeps that route URL and no helper methods of any visibility.
- **ONB-253** Each test declares its own data (names, documents, tokens), builds its own payload, makes its own call (REST Assured) and writes its own assertions inline, even when that repeats code across tests.
- **ONB-254** Stateless tools (`JsonMapper`) are fine as instance fields, and `BaseIntegrationTest` keeps its shared constants and fields (URL, port, tokens).
- **ONB-255** Request payloads in integration tests are declared in a single expression with `Map.of` and `List.of`, nested objects and lists included, never with `new HashMap<>()` plus `put`, `new ArrayList<>(List.of(...))` or JSON concatenated in a string.
- **ONB-256** Above 10 entries use `Map.ofEntries(Map.entry(...), ...)`. `HashMap` is only for a test that needs an explicit `null` value or a mutable map.
- **ONB-257** Each test generates its own data (UUIDs, random valid CPF and CNPJ); no teardown.
- **ONB-258** Random prefixes used to isolate a filter start with a fixed letter (`"x" + hex`), so they never match the digits of another test's CNPJ.
- **ONB-259** A filter test only counts if the data contains something the filter excludes (e.g. a `status=PENDING` filter never fails when every record is PENDING); assert the exact count (`hasSize`) of what the filter keeps.
- **ONB-260** Every route has success and error tests; a 409 on duplicates proves the write when there is no GET route yet.
- **ONB-261** Integration tests assert that a date field ends with `Z`.
- **ONB-262** No comments in test code (`//`, `/* */`, Javadoc).

## Shared helpers

- **ONB-263** Shared test helpers (`SetupTools`, `CpfGenerator`, `CnpjGenerator`, `MockTools`) live in `src/test/java/.../common/` and are the only reusable test code, plus the expectation templates in `src/test/resources/expectations/`.
- **ONB-264** The no-constants rule applies to them too, except for URLs and paths in `SetupTools`; stateless tools (`JsonMapper`) are fine as fields.
- **ONB-265** `SetupTools` only creates prerequisites for other scopes (e.g. creating an organization to test the GET route); it does not hold payload builders.
- **ONB-266** A `SetupTools` method is named after the action of the route it calls, never after the state it leaves behind, and a variant of the same route keeps that name plus the outcome (e.g. `createLegalPerson` and `createLegalPersonWithError`).
- **ONB-267** `SetupTools` methods (e.g. `createLegalPerson(ownerDocumentNumber)`) receive the data the test needs to control, make the call and have a single assertion, on the response status, so a broken prerequisite fails at the setup line instead of later in the test.
- **ONB-268** A test that needs a record that already exists creates it through `SetupTools`, never by relying on seed data from `database.sql` (the seeds are development fixtures, not test cases).
- **ONB-269** Documents are generated by one class per document in `common/` (`CpfGenerator.generateCpf()`, `CnpjGenerator.generateCnpj()`).
- **ONB-270** Test helpers are not named `<Subject>Handlers`, which belongs to the production `common/` and would clash with it (same package).

## Other

