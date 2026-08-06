# Database and migration report

Room contains profile, health events, structured medications, medication change history, documents, and reminders. Migrations 1→2 preserve the foundation; 2→3 adds medications; 3→4 adds documents; 4→5 adds reminders; 5→6 adds optional symptom timing/attribute columns; and 6→7 adds medication change history non-destructively. KSP, targeted database compilation, unit tests, clean builds, and connected checks pass. Dedicated migration fixtures and corruption/transaction tests remain open.
