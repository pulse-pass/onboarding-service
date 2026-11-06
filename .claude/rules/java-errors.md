---
description: Errors: DomainException, handler, codes and extra_fields
paths:
  - "**/exceptions/**"
  - "**/application/**"
  - "src/main/resources/messages*.properties"
---

# Errors

- **ONB-131** Handlers take no `Locale` parameter; `build` reads the request language from `LocaleContextHolder.getLocale()` (fed by `Accept-Language`) to pick `messages.properties` or `messages_pt.properties`.
- **ONB-132** Every error goes through `build` and answers `ErrorResponse(title, message, code, extra_fields)`; `extra_fields` is always present, an empty list when there is nothing to add.
- **ONB-133** Business exceptions extend `DomainException` (`getCode()`, `getMessageKey()`), mapped to an HTTP status in the `GlobalExceptionHandler` `STATUS_MAP`, with the message from the `MessageSource` (`messages.properties` in English and `messages_pt.properties`).
- **ONB-134** A schema validation failure (`MethodArgumentNotValidException`) answers 400 `GEN00400` with `ExtraFieldErrorResponse(field, error)` items where `field` is the JSON path in snake_case (e.g. `legal_person_collaborators[0].password`).
- **ONB-135** `error` is `REQUIRED` (`@NotBlank`, `@NotNull`), `INVALID_FORMAT` (`@Pattern`, `@Email`, `@Positive`, `@Digits`) or `SIZE` (`@Size`, `@Min`, `@Max`).
- **ONB-137** A violation on a `@RequestParam` (native method validation, `HandlerMethodValidationException`) answers 400 `GEN00400` with `[{"field": "page_size", "error": "SIZE"}]` (`ExtraFieldErrorResponse.fromParameterError`).
- **ONB-138** A parameter of the wrong type (`MethodArgumentTypeMismatchException`) answers `[{"field": "organization_status", "error": "INVALID_FORMAT"}]`.
- **ONB-139** An unreadable body or a value that does not parse, such as a value outside a schema enum (`HttpMessageNotReadableException`), answers `[{"field": "body", "error": "INVALID_FORMAT"}]`.
- **ONB-140** A missing required header (`MissingRequestHeaderException`) is handled by `handleMissingHeader` → 400 `GEN00400` with `[{"field": "<header-name>", "error": "REQUIRED"}]` (the header name as sent, not snake_case).
- **ONB-141** `GEN00400` always carries a non-empty `extra_fields`, and no other error uses `GEN00400`.
- **ONB-142** Never branch on types with `instanceof` (including pattern matching); write one method per type instead (e.g. one `@ExceptionHandler` per exception).
- **ONB-143** Application setup errors (e.g. a domain table enumerator not found) throw a generic exception (`IllegalStateException`) → 500 `GEN00500`.
- **ONB-144** Exception messages are plain literal text (e.g. `"Organization status PENDING not found"`), no string concatenation.
- **ONB-145** Generic errors (schema validation, authentication, permission, unknown route or method, unsupported media type and unexpected failures) use `GEN` + the HTTP status (`GEN00400`, `GEN00401`, `GEN00403`, `GEN00404`, `GEN00405`, `GEN00415`, `GEN00500`).
- **ONB-146** Authentication and permission exceptions (e.g. `UnauthorizedInternalRequestException`) are generic too: they use `GEN00401` / `GEN00403` and map to 401 / 403.
- **ONB-147** Every other `DomainException` carries the service code `<SVC>0000N` (e.g. `ORG00001`, `ONB00006`) and maps to a 4xx status in the `STATUS_MAP`.
- **ONB-148** Every `DomainException` must be mapped: the 500 default of `getOrDefault` exists only for a forgotten mapping, which is a bug.
- **ONB-149** 500 is always `GEN00500` with the generic `error.internal` message.
