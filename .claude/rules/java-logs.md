---
description: Log format, Map argument, S5145 and what never to log
paths:
  - "src/main/java/**/*.java"
---

# Logs

- **ONB-173** Logs follow `ClassName.method - message`.
- **ONB-174** The message is literal text; data goes in a single `Map` argument (`logger.error("X.y - Failed to create the user {}", Map.of("person_key", key), e)`), never interpolated in the sentence.
- **ONB-175** The exception is passed as the last argument, never `e.getMessage()`.
- **ONB-176** A log never receives a value that came straight from the request (path variable, query string, body), which Sonar flags as log injection (`javasecurity:S5145`) and fails the quality gate.
- **ONB-177** Log the value of the loaded entity (e.g. its `personKey`) instead, or leave it out when nothing was loaded yet.
- **ONB-178** In the application layer, every `if` that changes the flow has a log as its first line: `logger.warn` when it refuses the request, `logger.info` when it takes another path.
- **ONB-179** Passwords and tokens never appear in a log or a response, and the log `Map` never includes them.
- **ONB-180** Logs never receive a DTO, schema, response or entity: only the single `Map` with the chosen fields; records do not override `toString`.
- **ONB-181** Documents (CPF, CNPJ) and keys may be logged.
