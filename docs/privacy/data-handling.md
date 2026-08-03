# Data Handling

Status: Canonical. Purpose: define sensitive-data handling.

Keep health data in app-private storage, use Room/DataStore through repositories, and keep imported originals in protected private files. Never put health data in logs, analytics, screenshots, URLs, or public storage. Sharing requires an explicit user action and temporary grants.

