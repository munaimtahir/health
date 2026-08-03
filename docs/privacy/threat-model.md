# Threat Model

Status: Draft. Purpose: identify principal threats before production data flows.

Threats include device theft, unauthorized local access, leaked logs, public file exposure, backup tampering, corrupt imports, path traversal, MIME confusion, accidental deletion, and app-lock bypass. Mitigations are private storage, Keystore, authentication gates, opaque paths, validation, checksums, safe errors, confirmations, and migration/restore tests. Rooted-device guarantees remain unresolved and must not be overstated.

