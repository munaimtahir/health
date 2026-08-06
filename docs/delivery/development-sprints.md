# Development Sprints

Status: Canonical. Purpose: define staged delivery.

Sprint 0 is product constitution and documentation. Sprint 1 is the foundation shell now scaffolded. Sprint 2 adds onboarding/profile/app lock; subsequent sprints add timeline, symptoms, records, medications, reminders, reports/ownership, and release hardening. Do not advance past a sprint with a failed gate.

## Continuous completion hard rule

This sprint is one continuous execution sequence. A quality gate closes only the feature gap immediately preceding it; it must not stop the sprint. Once the gate passes, update the relevant documentation and begin the next item in `docs/verification/DEFERRED_ITEMS.md` without requesting a check-in or waiting for approval. Continue until there are no deferred engineering items, no remaining engineering gaps, and all release acceptance gates pass. If owner input is required, park only that exact step and continue unaffected work. The only permitted final deferred item is manual Play Console submission/publication by the owner.
