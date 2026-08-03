# File Storage Design

Status: Canonical. Purpose: define safe document handling.

The current `LocalSecureFileStore` copies approved PDF/JPEG/PNG bytes into app-private `files/documents` under UUID identifiers, preserves originals, validates MIME and a 50 MiB limit, and computes SHA-256 while copying. Invalid identifiers cannot be opened or deleted. Room metadata, SAF intake, FileProvider sharing, preview, and cleanup integration remain incomplete.
