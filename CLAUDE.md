# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository

The repository is named `health`; the public application is **Vexel Health Passport** (`com.vexel.passport`) — a private, single-user, offline-first Android health-record organizer. It is a runnable internal-testing build (Kotlin/Compose, multi-module Gradle), not a spec-only pack: `app/`, `core/*`, and `feature/*` all contain real source. Full acceptance/UI/security/accessibility/performance coverage is still incomplete (tracked under `docs/verification/`); don't claim release readiness without checking current gate status.

## Commands

Requires JDK 17, Android SDK 36, and the included Gradle wrapper (AGP 8.7.3, Kotlin 2.0.21). App module targets minSdk 26 / target+compileSdk 36.

```bash
./gradlew assembleDebug              # build debug APK
./gradlew assembleRelease bundleRelease   # build/bundle release (unsigned unless vexelRelease* gradle properties are set)
./gradlew test                       # all JVM unit tests
./gradlew :core:model:test           # unit tests for one module
./gradlew :core:model:test --tests "com.vexel.passport.core.model.SymptomValidationTest"  # single test class
./gradlew lint                       # Android lint (abortOnError = true)
./gradlew check                      # test + lint across modules
./gradlew connectedCheck             # instrumented tests; needs an attached device/emulator
./scripts/verify.sh                  # assembleDebug + test + lint (what CI runs)
./verify_project.sh                  # fuller local pass: clean/assemble/bundle/test/lint/check/verify.sh, writes logs to docs/verification/evidence/, adds connectedCheck if adb finds a device
```

There is no ktlint/detekt config; formatting is whatever `lint` and reviewer judgment enforce. CI (`.github/workflows/ci.yml`) runs `scripts/verify.sh` plus a doc-link sanity check (`rg` scan for placeholder link text like "latest"/"final-final") on every push and PR.

## Module architecture

Gradle Kotlin DSL, package root `com.vexel.passport` mirrored under every module (`core.*`, `feature.*`). Dependency direction is strictly `app → feature → core`, enforced by convention, not tooling:

- `app` is the only module that composes features, owns the `NavHost`/routes, and wires Hilt's `AppModule` (`app/src/main/kotlin/com/vexel/passport/di/AppModule.kt`).
- `feature/*` modules (`onboarding`, `dashboard`, `profile`, `timeline`, `symptoms`, `records`, `medications`, `reminders`, `appointments`, `reports`, `settings`) never depend on each other and never depend on `app`.
- `core/model` — pure shared data models, no Android/platform deps.
- `core/domain` — platform-light contracts/use cases.
- `core/database` — Room only (`HealthDatabase`, DAOs/entities for profile, health events, medications + medication changes, documents, reminders); migrations must be explicit, forward-only, and tested — destructive migration is forbidden in production.
- `core/security` — Keystore-backed PIN material cipher (`KeystorePinMaterialCipher`), `PinVerifier`, and `BackupCrypto` (PBKDF2-HMAC-SHA256 + AES-GCM, per-backup random salt/IV, password never persisted). No custom crypto beyond this — `SECURITY.md` explicitly forbids it.
- `core/files` — `SecureFileStore`/`LocalSecureFileStore`: copies validated PDF/JPEG/PNG into app-private `files/documents` under UUID identifiers, preserves original bytes, enforces MIME + 50 MiB limit, computes SHA-256.
- `core/datastore`, `core/notifications` (WorkManager reminder scheduling), `core/designsystem`, `core/ui`, `core/common`, `core/testing` (shared fakes, e.g. `FakeHealthData`).

Data flow (MVVM, unidirectional): Compose UI → ViewModel → domain use case → repository, which writes to Room/DataStore/file store and exposes `Flow` state back to the UI; a write must commit locally before the UI reports success. No core feature may require network access.

Entity relationships worth knowing before touching the schema: one profile owns allergies, diagnoses, procedures, symptoms, records, medications, consultations, reminders, appointments, reports, and backup metadata. Timeline events project source entities rather than copying mutable clinical content. A medication dose change ends the prior regimen and starts a new one — never mutate history in place.

## Non-negotiable product boundary

Vexel Health Passport must never diagnose, interpret lab/clinical results as a professional, recommend or prescribe treatment/medication changes, choose follow-up intervals, perform emergency triage, or state causation (use "occurred around the same time," never "caused by"). This overrides any feature request that conflicts with it. Required disclaimer wording lives in `docs/product/medical-safety-boundary.md`. Do not implement AI/OCR, telemedicine, multi-profile, cloud-first architecture, mandatory accounts, ads, or analytics — see `docs/product/deferred-features.md` / `docs/product/non-goals.md`. Any future AI/OCR extension point must be optional, on-device-by-default with explicit consent for external processing, present results as suggestions requiring confirmation, and never overwrite originals.

## Privacy and logging

Never log names, dates of birth, symptoms, medications, diagnoses, report content, filenames, paths, or identifiers — coarse event names and status codes only, and release builds must not carry verbose health-data logging (`docs/privacy/logging-policy.md`). Never use real patient data in code, tests, fixtures, screenshots, or commits — synthetic data only.

## Documentation authority

`docs/README.md` is the canonical documentation index (governance, product, design, architecture, data, privacy, testing, delivery, release, archive). When sources conflict, resolve in this order: fixed project decisions → current canonical `docs/` documents → ADRs (`docs/architecture/architecture-decisions/`) → approved entries in `docs/governance/decision-log.md` → the archived original spec pack (`docs/archive/original-ai-development-pack/`) → agent assumptions. Record any newly discovered contradiction in the decision log rather than resolving it silently. Before starting a sprint-sized change, check `docs/product/mvp-scope.md`, the relevant architecture/privacy docs, and the applicable gate in `docs/testing/quality-gates.md` / `docs/delivery/definition-of-done.md`.

## Quality priorities (in order)

No data loss > no privacy exposure > accurate allergy/medication reporting > reliable backup/restore > reliable reminders > low-burden symptom entry > accessibility > performance > visual polish.
