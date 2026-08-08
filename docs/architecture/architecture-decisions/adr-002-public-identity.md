# Repository Name Versus Application Name

Status: Accepted. Date: 2026-08-04.

## Context

The technical repository is named `health`, while the product has a fixed public identity.

## Decision

All user-facing surfaces use **Vexel Health Passport**; only technical paths and repository references may use `health`. The application ID is `com.vexel.passport`.

## Alternatives considered

Using `health` publicly would be ambiguous and violate the product identity.

## Consequences

String resources, docs, reports, and release copy require explicit review.

