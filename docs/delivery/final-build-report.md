# Final build report

## Project identity

- Repository: `health`
- Application: Vexel Health Passport
- Application ID: `com.vexel.passport`
- Version: `1.0.4` / version code `4` (per `app/build.gradle.kts`; not changed this session — no version bump policy violation)
- Minimum SDK: 26
- Target/compile SDK: 36

## Sprint results (prior sessions)

- Sprint 0: PASSED — repository audit, documentation archive, Gradle normalization, CI and verification foundation.
- Sprint 1: PASSED — Compose shell, Hilt, Room/DataStore infrastructure, onboarding/profile/event flows and connected settings coverage.
- Sprints 2–12: PASSED FOR INTERNAL TESTING — feature implementation, regression tests, release artifacts, full repository gates, and physical-device connected checks pass.

## Repair programme (this session, 2026-08-17 to 2026-08-21)

33 verified commits landed on `main`, each preceded by a green `assembleDebug`/`test`/`lint`/`check`/boundary-verification gate. Full list: `git log bbc88ac..HEAD --oneline`. Highlights:

- Fixed a non-buildable repo (missing Gradle wrapper).
- Real `NavHost` navigation; removed a duplicate Room database instance in reminder reconciliation.
- Fixed a real app-crash class: wrong backup password, corrupted backup, oversized/unsupported document import, and PIN-Keystore failures all previously threw uncaught exceptions inside `viewModelScope.launch`; now caught and mapped to safe user-facing messages.
- Strict date validation for symptom and medication dates (previously silently accepted malformed input).
- Timeline record-type filters, vault sort, overdue-reminder indicator, real PDF printing (`PrintDocumentAdapter`, not bitmap-only `PrintHelper`), WEEKLY/MONTHLY recurring reminders, offline in-app Help and Privacy & Safety viewers, optional `FLAG_SECURE` screenshot protection.
- Enabled Room `exportSchema` and added migration-test tooling for future migrations (see `docs/delivery/review-findings-status.md` for why the v1→v8 chain itself isn't retroactively covered).
- First-class Consultation/Procedure record kinds (no schema change — see decision D-010); bounded vault document thumbnails with disposable-cache cleanup on delete/replace/full-deletion; Undo for archiving a timeline record; immediate purge of temporary share-cache files on full data deletion; fixed several form fields (Profile edits, symptom/event capture, report options) that silently discarded in-progress user input on rotation because they used `remember` instead of `rememberSaveable`.

See `docs/delivery/review-findings-status.md` for the full per-finding breakdown and `docs/delivery/deferred-owner-decisions.md` for parked items.

## Verification

- This session: `./gradlew assembleDebug`, `./gradlew assembleRelease` (unsigned — no `vexelRelease*` properties in this environment), `./gradlew bundleRelease` (unsigned), `./gradlew test`, `./gradlew lint`, `./gradlew check`, and `scripts/verify_boundaries.sh` all pass at HEAD. `connectedCheck`/instrumented tests were NOT run — no ADB device/emulator attached in this environment (owner-confirmed plan: run device verification after cloning this branch onto a device with ADB access).
- Prior session (`docs/delivery/build-progress.md`, `docs/delivery/quality-gate-results.md`): `./verify_project.sh` including six connected app tests passed on a physical TECNO CH6i (Android 13). That device is not available in this session's environment.
- Debug artifact: `app/build/outputs/apk/debug/app-debug.apk` after the last `assembleDebug` in this session.
- Release APK/AAB produced this session are **unsigned** (no signing credentials in this environment); the prior session's signed artifacts are historical and not reproduced here.

## Known limitations

The current build is suitable for controlled internal testing. Play Console submission/publication remains a manual owner action. Device-based verification (connected tests, manual runtime smoke test, biometric prompt, Macrobenchmark) is deferred to the planned post-development device checkup. Signed release artifacts require owner-supplied signing credentials outside this repository.

## Parked blockers

See `docs/delivery/deferred-owner-decisions.md`: DOD-001 (device verification, deferred to post-development checkup), DOD-002 (release signing credentials), DOD-003 (Play Console access, external/owner-only), DOD-004 (default value for the opt-in screenshot-protection preference).

## Final status

`FUNCTIONALLY COMPLETE — ENVIRONMENTAL VERIFICATION PENDING`

This session closed a real crash-class defect (unguarded exceptions in backup/restore/import/PIN coroutines), several accuracy/validation gaps, and multiple explicitly-required Phase 3–6 features (printing, in-app Help/Privacy viewers, WEEKLY/MONTHLY reminders, overdue indicators, migration-test tooling), all gate-verified. It did not attempt the largest, highest-regression-risk architectural items (extracting real code into the `feature/*` placeholder modules, Paging3 for the timeline, a full PDF layout engine rewrite, structured consultations/procedures requiring a schema migration, string/localization extraction, convention plugins, Macrobenchmark/baseline profiles) — each is a genuinely multi-hour effort in its own right, and several (the schema migration, the module extraction) carry real data-loss/regression risk that deserves device-based verification this environment cannot provide. These are documented, not silently dropped.
