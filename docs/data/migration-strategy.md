# Migration Strategy

Status: Canonical. Purpose: protect data through releases.

Room migrations are explicit, forward-only, and tested from every supported version. Destructive migration is not production default. Backup manifests carry schema version, and restore validates compatibility before writing. Migration failures preserve the prior usable state where possible.

