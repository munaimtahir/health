# Changelog

All notable changes to Vexel Health Passport are documented here.

## [Unreleased]

- Organized the original AI development pack into canonical documentation.
- Added the modular Android Compose scaffold and offline-first foundations.
- Fixed a missing Gradle wrapper that made the repository non-buildable from a clean clone.
- Replaced ad hoc tab-index navigation with a real `NavHost`/`NavController`.
- Removed a duplicate Room database instance opened during reminder reconciliation.
- Fixed backup/restore/report/document-import/PIN-setup failures crashing the app instead of showing a safe error message.
- Added strict date validation for symptom and medication entry (previously silently accepted malformed dates).
- Added timeline record-type filters, document vault sort (date/category/type), an overdue-reminder indicator, real multipage PDF printing, WEEKLY and MONTHLY recurring reminders, offline in-app Help and Privacy & Safety viewers, and an optional screenshot/recent-apps privacy protection.
- Enabled Room schema export and added migration-test tooling for future schema changes.

