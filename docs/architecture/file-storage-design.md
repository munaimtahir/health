# File Storage Design

Status: Canonical. Purpose: define safe document handling.

The current `LocalSecureFileStore` copies approved PDF/JPEG/PNG bytes into app-private `files/documents` under UUID identifiers, preserves originals, validates MIME and a 50 MiB limit, and computes SHA-256 while copying. It supports in-place replacement with recalculated metadata, and invalid identifiers cannot be opened or deleted. Room metadata, SAF intake, FileProvider opening/sharing, and replacement are implemented; a dedicated visual preview and broader cleanup/device matrix remain open.
