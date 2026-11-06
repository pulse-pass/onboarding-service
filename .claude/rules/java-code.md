---
description: Java code style: method order, flow, naming, ifs, catch, comments
paths:
  - "src/main/java/**/*.java"
---

# Java code

## Method order and flow

- **ONB-115** Private methods always before public ones, in the order they are used.
- **ONB-116** In services and controllers, public methods go: create, then update, then delete, then get/list.
- **ONB-117** Private methods only for real auxiliary steps used by several public methods or with a clear business name of their own (e.g. `saveLegalPerson`).
- **ONB-118** The flow of a public method stays inline and linear, in the method itself: never extract its branches or its save, history and commit sequence to a private method (`approveOrganization`, `saveAndCommit`), even when lines repeat, and never to shorten the method.
- **ONB-272** A get service builds its result from plain lookups in sequence, never branching on a type to decide what to load: what does not apply comes back `null`.
- **ONB-119** Lookups of domain tables by enumerator (`findByEnumerator(...).orElseThrow(...)`) stay inline in the public method, even when repeated, and are never extracted to a private method.
- **ONB-120** Prefer separate, clear `if`s over compound `||` conditions when each case has its own meaning.
- **ONB-121** One `catch` per different handling: two `catch` blocks that do the same thing become a single `catch (Exception ex)`. A separate `catch` exists only when that exception has specific handling.
- **ONB-122** `try/catch` only around the call that actually throws (e.g. the `save` hitting a `UNIQUE` → `DataIntegrityViolationException` becomes the domain duplicate exception).

## Style

- **ONB-123** No defensive validation (e.g. checking that a value from the environment is not empty).
- **ONB-124** Keep it simple: compare strings with `equals`, no `MessageDigest.isEqual` or similar tricks.
- **ONB-125** No comments in Java code (Javadoc included): names and structure must explain the code, and decisions go to the rules, the README or the task notes.

## Conversions and naming

- **ONB-126** Static factories follow `from<Source>` (`fromEntity`, `fromDomain`, `OrganizationHistory.fromOrganization`), not `create`.
- **ONB-127** `to*`/`from*` methods exist only in classes that represent data, converting themselves: `toDTO()` in schemas, `toDomain()` and static `fromDomain(...)` in JPA entities, `toHistoryData()` in domain entities.
- **ONB-128** Static `from*` also exist in responses and history entities (`fromEntity`, `fromOrganization`), plus the static helpers of `common/` (`JsonHandlers.toJson`, `toSnakeCase`).
- **ONB-129** Services, gateways, controllers and other components never have a `to*`/`from*` method; `to*` is reserved for conversions of the object itself and static helpers, never for a private step that validates.
- **ONB-130** A private step that checks a value is `validate*` and returns nothing (e.g. `validateTokenResponse`); one that builds something is named after the action (`fetch*`, `build*`).
