# Rollback Plan

Status: Draft. Purpose: define response to unsafe releases.

Pause rollout, assess data-loss/privacy impact, communicate clearly, and ship a tested corrective build. Preserve evidence without collecting extra health data. Database changes require a forward-safe migration or a verified restore path; do not rely on destructive rollback.

