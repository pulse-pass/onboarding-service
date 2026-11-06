---
description: CI, main ruleset, SonarQube and branch conventions
paths:
  - ".github/**"
---

# Pipeline

- **ONB-216** Polyrepo in the `pulse-pass` GitHub organization, `main` only.
- **ONB-217** PRs come from `feature/<ID>-<slug>`; merge commits only (squash and rebase disabled).
- **ONB-218** `main` ruleset: active on the default branch, no bypass, blocks deletion and force push.
- **ONB-219** The ruleset requires a PR (0 approvals, merge method `merge`).
- **ONB-220** The ruleset requires every CI job to pass (`lint`, `unit-tests`, `integration-tests`, `sonar`, `build`).
- **ONB-221** It can be created with `gh api -X POST repos/pulse-pass/<repo>/rulesets`.
- **ONB-222** SonarQube Cloud: organization `pulse-pass` (Free plan), project `pulse-pass_<repo>` with GitHub Actions analysis.
- **ONB-223** `SONAR_TOKEN` is an organization secret shared by all repositories.
- **ONB-224** Workflows: `pull-request.yml` (name `CI`) and `publish.yml` (name `Publish`, never "release").
- **ONB-225** Third-party actions are pinned by full commit SHA (Sonar rule S7637) with the version in a comment; `actions/*` may use tags. That version is the only comment allowed in workflows.
- **ONB-226** The `sonar` job runs with `-Dsonar.qualitygate.wait=true`, so a failed quality gate fails the job and skips `build`.
- **ONB-227** Pipeline: PR with lint (Checkstyle) → unit tests (JaCoCo) → integration tests (through `service-start.sh test`) → SonarQube Cloud → image build.
- **ONB-228** On merge: version from the commits, `vX.Y.Z` tag and image on GHCR.
