# Contributing to Vexel Health Passport

Status: Active.

## Workflow

Create focused branches from the default branch and keep changes small. Use imperative commit messages with a conventional prefix where useful, for example `feat: add dashboard shell` or `docs: update threat model`. Do not push or add release credentials from local work.

## Review expectations

Pull requests must explain scope, link the canonical requirement or decision, list verification commands and results, and include screenshots for UI changes. Reviewers check module boundaries, accessibility, privacy, medical-safety wording, and scope protection.

## Verification

Run `./gradlew assembleDebug test lint` and the relevant UI/device checks. Update canonical docs when behavior, data, permissions, dependencies, or user-facing copy changes. Never include real patient data in tests, screenshots, logs, or fixtures.

Report vulnerabilities privately according to [SECURITY.md](SECURITY.md).

