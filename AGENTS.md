# Repository Guidelines

## Project Structure & Module Organization

This repository currently contains the Vexel Health Passport product and engineering specification pack, not application code. The numbered Markdown files at the repository root are the source requirements; `manifest.json` lists the pack contents, and `12_VISUAL_MARKET_AND_UI_CONCEPT.png` is the visual reference. `docs/` is reserved for the future canonical documentation structure. When implementation begins, use the planned Kotlin/Compose layout: `app/`, `core/` for shared infrastructure, and `feature/` for independent feature modules. Keep tests beside their modules in `src/test` and `src/androidTest`, and keep private health data out of the repository.

## Build, Test, and Development Commands

There is no build system yet, so Gradle commands are not currently available. After the Android scaffold is added, use:

```bash
./gradlew assembleDebug   # Build the debug APK
./gradlew test            # Run JVM unit tests
./gradlew lint            # Run Android lint
./gradlew connectedCheck  # Run device/emulator tests when available
```

Run the applicable checks before submitting changes. Do not commit generated build outputs or local SDK configuration.

## Coding Style & Naming Conventions

Use Kotlin with four-space indentation, standard Kotlin naming (`PascalCase` types, `camelCase` members, `UPPER_SNAKE_CASE` constants), and Compose conventions. Use lowercase kebab-case for documentation filenames. Prefer immutable state, explicit domain boundaries, and small composables. Apply the project formatter and lint configuration once the Gradle scaffold exists; avoid wildcard imports and unused dependencies.

## Testing Guidelines

The planned stack uses JUnit, AndroidX Test, Room/DataStore tests, and Compose UI tests. Name tests for observable behavior, for example `DashboardScreenTest` and `database_opens_with_version_one`. Cover navigation, theme rendering, persistence, migrations, and privacy-sensitive flows. Never use real patient data in fixtures, screenshots, or logs.

## Commit & Pull Request Guidelines

Existing history is minimal (`initial` commits), so no established convention can be inferred. Use concise imperative messages with a conventional prefix where practical, such as `feat: add dashboard shell` or `docs: normalize product requirements`. Pull requests should describe scope, link relevant requirements or issues, include verification commands and results, and attach screenshots for UI changes. Call out security, privacy, migration, or medical-safety implications explicitly.

## Architecture & Security Notes

Treat `CLAUDE.md` and the numbered specification pack as implementation guidance until canonical docs replace them. Preserve the non-diagnostic product boundary. The planned app is offline-first, single-user, and local-storage oriented: do not add analytics, advertising, mandatory accounts, cloud sync, or network-dependent core behavior without an explicit decision record. Never commit secrets or health information.
