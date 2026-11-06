---
description: UnitOfWork, repositories, locking, LAZY and secrets in the database
paths:
  - "**/application/**"
  - "**/domain/**"
  - "**/infrastructure/jpa/**"
  - "**/infrastructure/security/**"
---

# Persistence

## Transactions

- **ONB-182** Explicit transaction control through the `UnitOfWork` (request-scoped `JpaUnitOfWork`), never `@Transactional`.
- **ONB-183** Reads go before `unitOfWork.begin()`, validations first (e.g. duplicate checks) and only then the lookups the writes need (e.g. the PENDING status); `begin()`/`commit()` wraps only the writes.
- **ONB-184** No manual rollback in services: the `JpaUnitOfWork` `cleanup()` rolls back at the end of the request.
- **ONB-185** Updating an existing record: the lookup of the item is `findForUpdateBy<Key>`, with the same name in the domain repository, the `*RepositoryImpl` and the `Jpa*Repository`.
- **ONB-186** In the `Jpa*Repository` it is a derived query with `@Lock(LockModeType.PESSIMISTIC_WRITE)` and no `@Query`; `ForUpdate` goes before `By` because Spring Data reads everything after `By` as criteria.
- **ONB-187** The lookup with the lock is the first call after `unitOfWork.begin()`.
- **ONB-188** The validations that depend on the item come right after it, inside the transaction (the `cleanup()` rolls back).
- **ONB-189** Lookups of domain tables still go before `begin()`.

## Repositories

- **ONB-190** Repositories use only derived queries (no `@Query`/JPQL, no `@EntityGraph`).
- **ONB-191** Each repository returns the entity of its own table with `toDomain()` already assembling the relationships; the service uses what the domain loaded, without fetching the same entity again.
- **ONB-192** Always take the simplest path: plain derived lookups in sequence (e.g. the person by key, then the collaborator by key and role), no custom queries, extra abstractions or redundant calls when a direct one solves it.
- **ONB-193** `*RepositoryImpl` calls the plain `jpaRepository.save` (no `saveAndFlush`). With `AUTO_INCREMENT` the INSERT runs on `save`, inside the transaction.
- **ONB-194** When updating a loaded object, call `repository.save(object)` and keep using the same object: never capture the return in a variable such as `saved` or `failed`.
- **ONB-195** The return of `save` is used only when creating, to get the generated data.
- **ONB-196** Existence checks use `findBy...().isPresent()`, never `existsBy...` methods.

## Lazy loading

- **ONB-197** Relationships are `FetchType.LAZY`; `spring.jpa.open-in-view` stays at the Spring Boot default (enabled), so `toDomain()` can load them during the HTTP request even outside `begin()`/`commit()`.
- **ONB-198** Non-HTTP entry points (Kafka consumers) must read lazy relationships inside a transaction.
- **ONB-199** List routes keep `FetchType.LAZY` too: the page comes in a single query and each relationship loads lazily in `toDomain()`; the persistence context fetches each distinct related row once (type tables have few rows), so no `@EntityGraph`/`JOIN FETCH` for them.
- **ONB-200** Revisit only when a relationship points to data that differs per item (e.g. each company's owner).

## Secrets and uniqueness

- **ONB-201** The email lives in `legal_person` (unique, `legal_person_email_unique`, because the payment provider refuses a repeated one) and in `natural_person` (can repeat: the company and its owner can use the same one, and so can two owners).
- **ONB-202** The service checks the company email after the documents and answers 409 `ONB00010`.
