---
description: List routes, filters, Specification and pagination
paths:
  - "**/api/**"
  - "**/application/**"
  - "**/infrastructure/jpa/**"
---

# List routes

- **ONB-048** List routes: each filter is a loose `@RequestParam` (no query schema), plus `page` (default 1, `@Min(1)`) and `page_size` (default 30, `@Min(1)` `@Max(100)`) validated on the `@RequestParam` itself with native method validation.
- **ONB-049** The service `list<X>s(filters..., page, pageSize)` returns a `List` from `xRepository.findAll(filters..., page, pageSize)`.
- **ONB-050** The `*RepositoryImpl` builds `<X>Specification.withFilters(...)` (in `infrastructure/jpa/specifications`), starting from `criteriaBuilder.conjunction()` and adding one predicate per filter given: one filter per parameter.
- **ONB-051** Use `equal` for exact values (documents, keys, enumerators) and a prefix `like` with escape only for free text (the name), with the `like` overload with the escape character `\`, escaping `\`, `%` and `_` in the value.
- **ONB-052** Never a generic search parameter that looks in several columns, and never `like '%x%'` (a contains search does not use an index).
- **ONB-053** The `*RepositoryImpl` calls `jpaRepository.findAll(spec, PageRequest.of(page - 1, pageSize, Sort.by(...)))`, so the `Jpa*Repository` also extends `JpaSpecificationExecutor`.
- **ONB-054** Every list declares its order in the `PageRequest` (e.g. `Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))`), with `id` as the last tie-breaker.
- **ONB-055** One `findAll` serves every list of the resource, and a list that does not use a filter passes `null`.
- **ONB-056** The response is the generic `PaginatedResponse<XResponse>(data, PaginationResponse(page, page_size))`, both in `api/responses/pagination/`.
- **ONB-057** The item is the route's `List<X>sResponse` in `api/responses/` (e.g. `PaginatedResponse<ListOrganizationsResponse>`); objects nested inside it go to `definitions/`.
- **ONB-058** A list filter with fixed values (the enumerator of a domain table) is received as the domain enum in the `@RequestParam` (e.g. `OrganizationStatusEnum organizationStatus`).
- **ONB-059** The service, repository and Specification compare the `enumerator` with `organizationStatus.name()`.
- **ONB-061** A domain table list (e.g. `GET /<service>/internal/organization_rejection_reasons`) is a list route without filters, in its own controller and service named after the table, paginated like the other lists.
- **ONB-062** A domain table list is ordered by `id` (`Sort.by(Sort.Order.asc("id"))`) and the item exposes the `enumerator` under the table name and the `<table>_description`.
- **ONB-063** A list route resolved by the token reads `selected-agent-key` and always filters by it.
- **ONB-065** An item fetched by its own key returns the object or 404; with `selected-agent-key`, a key different from the token's also answers 404 (same `<X>NotFoundException`, never revealing the existence).
