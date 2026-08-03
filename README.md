# Vexel Health Passport

Your health history, organized.

The repository is technically named `health`; the public application name is **Vexel Health Passport**. This project is a private, single-user, offline-first Android health-record organizer. It stores user-entered information and does not diagnose, prescribe treatment, interpret results as a clinician, or perform emergency triage.

## Status

The current implementation is a runnable foundation shell. It provides Home, Timeline, Records, Plan, and Profile placeholders, a reusable Material 3 design system, Room version 1 infrastructure, DataStore preferences, security/file abstractions, tests, and CI. Production feature workflows are intentionally deferred.

## Stack and structure

Kotlin, Jetpack Compose, Material 3, Navigation Compose, Hilt, Room, DataStore, Coroutines/Flow, WorkManager, Android Keystore abstractions, SAF-ready file contracts, and Gradle Kotlin DSL. Features live under `feature/`; shared capabilities live under `core/`; `app/` owns composition and navigation.

## Build and verify

Requires JDK 21, Android SDK 36, and the included Gradle wrapper.

```bash
./gradlew assembleDebug
./gradlew test
./gradlew lint
./scripts/verify.sh
# With an emulator/device:
./gradlew connectedCheck
```

## Privacy and safety

Core functionality requires no network, account, analytics, advertising, or health-data telemetry. Sensitive data must remain private and out of logs. Read [docs/privacy/security-principles.md](docs/privacy/security-principles.md) and [docs/product/medical-safety-boundary.md](docs/product/medical-safety-boundary.md).

## Documentation

Start at [docs/README.md](docs/README.md). Canonical requirements, architecture, privacy, testing, delivery, and release documents are under `docs/`; original source files are preserved under [docs/archive/original-ai-development-pack/](docs/archive/original-ai-development-pack/).

## Roadmap

The next development phase is Sprint 1 — onboarding, privacy consent, personal health profile, and application lock. Do not start production feature work without updating the applicable canonical docs and gates.

