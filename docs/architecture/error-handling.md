# Error Handling

Status: Canonical. Purpose: define safe failure behavior.

Use typed domain errors and user-readable recovery guidance. Do not expose stack traces, private paths, filenames, or health values. Log only privacy-safe diagnostics. File corruption, denied permissions, unavailable biometrics, migration failures, and empty data are explicit states.

