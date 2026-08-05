# Data Handling

Status: Canonical. Purpose: define sensitive-data handling.

Keep health data in app-private storage, use Room/DataStore through repositories, and keep imported originals in protected private files. The document store uses opaque UUID filenames and SHA-256 integrity metadata; metadata editing, replacement, and explicit FileProvider sharing use temporary grants. Never put health data in logs, analytics, screenshots, URLs, or public storage. Backup encryption policy, sharing lifecycle testing, and Data Safety declarations still require completion.
