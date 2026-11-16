---
description: Status change and logical removal routes
paths:
  - "**/api/**"
  - "**/application/**"
---

# Status routes

- **ONB-066** Status change route: `PATCH /<service>/internal/<resources>/{<resource>_key}` takes `<x>_status` and accepts only the allowed target statuses (a schema enum in `definitions/`).
- **ONB-067** A status change is accepted only from the allowed source status, otherwise 422 with the status-not-allowed exception.
- **ONB-068** An option tied to the target (e.g. the rejection reason) is validated in the service and answers 422 with its own exception when missing or not allowed.
- **ONB-069** The response carries only the key, the status and the fields tied to it (e.g. the rejection reason and its description), with no timestamps.
- **ONB-070** Logical removal is `DELETE` on the same item (status `DELETED`, only from the allowed source status), answering 204 without a body.
- **ONB-071** Both routes look the item up by key with the lock (`findForUpdateBy<Key>`, the not-found exception answering 404) and check the source status before the option, inside the transaction.
- **ONB-072** Every change writes one `<x>_history` row with the old status.
- **ONB-275** `DELETE /onboarding/internal/legal_persons/{legal_person_key}/collaborators` revokes the access of a removed organization: it marks every `ACTIVE` collaborator `DELETED` (with history) and deactivates each user in the identity provider, answering 204; with no `ACTIVE` collaborator it answers 204 without calling the provider, so a repeated call is safe.
- **ONB-276** Each collaborator is committed on its own (ONB-279): a provider failure answers 422 `UserDeactivationException`, the collaborator being processed stays `ACTIVE` and the ones before it stay `DELETED` and deactivated, so repeating the call handles only what is left.
