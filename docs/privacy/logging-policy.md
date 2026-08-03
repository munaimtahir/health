# Logging Policy

Status: Canonical. Purpose: prohibit health-data leakage.

Do not log names, dates of birth, symptoms, medications, diagnoses, report content, filenames, paths, identifiers, or free text. Diagnostics must use coarse event names and safe status codes. Release builds must retain no verbose health-data logging.

