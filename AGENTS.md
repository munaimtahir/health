# Repository Guidelines

## Project Structure & Module Organization

Vexel Health Passport (`com.vexel.passport`) is a multi-module Kotlin/Compose Gradle project, not just a spec pack. `app/` composes features and owns navigation/DI; `core/{common,database,datastore,designsystem,domain,files,model,notifications,security,testing,ui}/` hold shared infrastructure; `feature/{onboarding,dashboard,profile,timeline,symptoms,records,medications,reminders,appointments,reports,settings}/` are independent feature modules that never depend on each other. `docs/` is the canonical documentation set (see `docs/README.md` for the authority order); the original numbered spec pack is preserved verbatim under `docs/archive/original-ai-development-pack/` and is no longer the source of truth. Keep tests beside their modules in `src/test` and `src/androidTest`, and keep private health data out of the repository.

## Build, Test, and Development Commands

```bash
./gradlew assembleDebug   # Build the debug APK
./gradlew test            # Run JVM unit tests
./gradlew :core:model:test --tests "com.vexel.passport.core.model.SymptomValidationTest"  # single test
./gradlew lint            # Run Android lint
./gradlew connectedCheck  # Run device/emulator tests when available
./scripts/verify.sh       # assembleDebug + test + lint, what CI runs
```

Run the applicable checks before submitting changes. Do not commit generated build outputs or local SDK configuration.

## Coding Style & Naming Conventions

Use Kotlin with four-space indentation, standard Kotlin naming (`PascalCase` types, `camelCase` members, `UPPER_SNAKE_CASE` constants), and Compose conventions. Use lowercase kebab-case for documentation filenames. Prefer immutable state, explicit domain boundaries, and small composables. There is no ktlint/detekt config; follow existing module style and avoid wildcard imports and unused dependencies.

## Testing Guidelines

JUnit, AndroidX Test, Room/DataStore tests, and Compose UI tests. Name tests for observable behavior, for example `DashboardScreenTest` and `database_opens_with_version_one`. Cover navigation, theme rendering, persistence, migrations, and privacy-sensitive flows. Never use real patient data in fixtures, screenshots, or logs.

## Commit & Pull Request Guidelines

Follow the established convention: imperative, conventional-prefix subject lines such as `feat: add explicit symptom episode history`, `fix: validate report selection and date ranges`, `test(device): record complete synthetic feature smoke`, `docs: finalize internal testing handover`. Pull requests should describe scope, link relevant requirements or issues, include verification commands and results, and attach screenshots for UI changes. Call out security, privacy, migration, or medical-safety implications explicitly.

## Architecture & Security Notes

Treat `CLAUDE.md` and `docs/` (see the authority order in `docs/README.md`) as implementation guidance. Preserve the non-diagnostic product boundary (`docs/product/medical-safety-boundary.md`, `docs/product/non-goals.md`). The app is offline-first, single-user, and local-storage oriented: do not add analytics, advertising, mandatory accounts, cloud sync, or network-dependent core behavior without an explicit decision record in `docs/governance/decision-log.md`. Never commit secrets or health information, and never log names, dates of birth, symptoms, medications, diagnoses, filenames, or paths (`docs/privacy/logging-policy.md`).

## Mandatory continuous-completion rule

The active implementation sprint must continue until every engineering item in `docs/verification/DEFERRED_ITEMS.md` is implemented, tested, documented, and passed through the applicable quality gate. Completing one gap and passing its gate is a transition into the next gap, not a stopping point. After each closure, update the documentation and immediately select the next remaining gap. Do not pause for owner input; if a step requires unavailable input, skip only that step, record it precisely, and continue all other engineering work. The only permitted final deferred item is Play Console submission/publication, which is a manual owner action.
