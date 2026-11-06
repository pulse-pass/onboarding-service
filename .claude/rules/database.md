---
description: MySQL modeling conventions
paths:
  - "infrastructure/database.sql"
  - "src/main/java/**/infrastructure/jpa/**"
---

# Database

- **ONB-001** One schema per service, named after the service without a `_db` suffix (`organization`, `onboarding`, `event`, `purchase_order`, `webhook`, `notification`); the local database has the same name.
- **ONB-002** Tables and columns in snake_case and singular; compound names when a single word is too generic.
- **ONB-003** `id BIGINT AUTO_INCREMENT` as the internal PK (keep the `AUTO_INCREMENT`) and a unique `<table>_key VARCHAR(36)` (UUID) used by the API and between services.
- **ONB-004** Exception: 1:1 specialization tables (`natural_person`, `legal_person`) have no `<table>_key` of their own; the public key is the `person_key` of `person`.
- **ONB-005** Column order: id, public key, references, type, own data, amounts, dates, JSON, status, `created_at`/`updated_at` (`TIMESTAMP(3)`).
- **ONB-006** Types, statuses and predefined options (e.g. rejection reasons) are domain tables `<x>_type`, `<x>_status` and `<x>_<option>` with an `enumerator` (plus `description` when displayed), seeded with `INSERT IGNORE` and referenced by FK; never free text.
- **ONB-007** `<x>_status` tables only have `created_at`.
- **ONB-008** Every entity with a status has `<x>_history` (`<x>_history_key`, `<x>_id`, `old_status_id` null on creation, `new_status_id`, `history_data` JSON, only `created_at`).
- **ONB-009** Monetary values are `DECIMAL(19,2)` named `amount` (or `<x>_amount`).
- **ONB-010** External keys are `external_*_key VARCHAR(36)`.
- **ONB-011** Named constraints: `pk_<table>`, `<table>_<column>_unique`, `fkN_<table>`, `<table>_<column>_index`.
- **ONB-012** A column used as a list filter gets an index (`INDEX <table>_<column>_index (<column>)`) unless a `UNIQUE` or FK already covers it.
- **ONB-014** No relationships across schemas: reference by `<x>_key`; when a service must filter or search by another service's data, it keeps a copy (e.g. CNPJ and company name on `organization`).
- **ONB-015** No migrations: `database.sql` is idempotent (`CREATE SCHEMA/TABLE IF NOT EXISTS`, `INSERT IGNORE` on tables with a unique enumerator) and has no credentials.
- **ONB-016** The development admin seeded in the Keycloak realm (`52998224725`, `person_key` `00000000-0000-4000-8000-000000000001`) also has `person` and `natural_person` rows in `database.sql`, so the `selected-user-key` of the admin token resolves to a person.
- **ONB-017** The admin rows use `INSERT IGNORE` (idempotent through the `person_key` and `document_number` unique constraints); keep both seeds in sync.
