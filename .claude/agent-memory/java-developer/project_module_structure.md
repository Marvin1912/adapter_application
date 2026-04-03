---
name: Post-refactor module structure
description: costs and backup modules replaced database/importer; which modules depend on what for JPA/Flyway
type: project
---

The `database` and `importer` modules were removed as part of GitHub issue #79 (2026-04-03).

- `costs` module: holds all cost-related logic (DailyCost, MonthlyCost, Salary, SpecialCost), repositories, importer services, Flyway for `finance` schema (`db/migration/costs`), infrastructure (Ibans/Consul), DirectoryWatcher, Delegator, GenericFileReader
- `backup` module: holds backup upload logic (BackupDirectoryWatcher, BackupUploadHandler, BackupTrackingService), BackupRunRepository, Flyway for `exports` schema (`db/migration/exports`)

**Why:** Both had JPA/Flyway/Hibernate-Envers declared as `api` deps — these transitively satisfy JpaRepository usage in feature modules (plants, image-server, it-news, mental-arithmetic).

**How to apply:** Feature modules that previously depended on `:database` now depend on `:costs` for JPA/Flyway transitive deps. The `vocabulary` module depends on `:backup` (uses BackupRunRepository for Anki sync tracking). The `api` module depends on both `:costs` and `:backup`.

Pre-existing checkstyle failures exist in: `camt`, `consul`, `entities`, `image-server`, `vocabulary` — these are NOT introduced by this refactoring.
