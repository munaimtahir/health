# Dependency Rules

Status: Canonical. Purpose: make module checks reviewable.

Allowed direction is `app → feature → core`, with `core:model` and `core:common` at the base. Feature-to-feature dependencies and core-to-app dependencies are forbidden. Platform implementations stay behind domain contracts. New dependencies require a documented purpose and version-catalog entry; unused dependencies are removed.

