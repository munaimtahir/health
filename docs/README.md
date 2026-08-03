# Vexel Health Passport Documentation

Status: Canonical.

## Purpose

This is the authoritative documentation set for Vexel Health Passport, a private, offline-first personal health-record application. The repository name is `health`; the public application name is always **Vexel Health Passport**.

## Organization

- `governance/` — authority, terminology, decisions, and maintenance.
- `product/` — purpose, requirements, safety boundary, scope, and roadmap.
- `design/` — interaction, visual, accessibility, and content guidance.
- `architecture/` — modules, data flow, persistence, security boundaries, and ADRs.
- `data/` — entities, retention, import/export, and migrations.
- `privacy/` — privacy, threat, permissions, logging, and policy preparation.
- `testing/` — quality gates and test matrices.
- `delivery/` — sprint and review procedures.
- `release/` — store and release readiness.
- `archive/` — preserved source material; not authoritative by itself.

## Authority and contradictions

Resolve requirements in this order: fixed project decisions, current canonical documents, ADRs, approved decision-log entries, archived pack, then agent assumptions. Important contradictions must be recorded in [`governance/decision-log.md`](governance/decision-log.md), never silently resolved.

Before each sprint, the coding agent reviews `product/mvp-scope.md`, the relevant acceptance criteria, the applicable architecture and privacy documents, and the sprint gate. Implementation changes require a same-change documentation update when behavior, data, permissions, security, or user-facing wording changes.

