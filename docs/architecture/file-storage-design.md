# File Storage Design

Status: Canonical. Purpose: define safe document handling.

Future PDF/image/camera/SAF/share intake copies bytes into app-private storage under opaque identifiers, preserves originals, validates MIME and size, stores metadata in Room, and uses checksums for duplicate detection. Original files are never silently rewritten and are not exposed through public storage.

