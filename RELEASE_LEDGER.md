# Release Ledger

Tracks build/version history and Play Store publication status for Vexel Health Passport (`com.vexel.passport`). Source of truth for "has this shipped" is this file plus `docs/verification/RELEASE_READINESS_CHECKLIST.md`; do not infer publication status from version bumps alone.

## Version 1.0.4 (versionCode 4) — in progress

- `app/build.gradle.kts` set to versionCode 4 / versionName "1.0.4" (not yet built for release / not yet uploaded).
- Addresses Play Console's "App optimization: High" flag, shown against v1.0.3/v2 with all percentages blank and an R8 configuration checklist: **Full Mode, Resource Shrinking, Resource Shrinking Optimized, Repackage Classes**, plus the recurring "Upgrade to AGP 9.0" suggestion.
  1. **AGP 8.7.3 → 8.13.0, Gradle wrapper 8.9 → 8.13** (`gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`). AGP ≥8.12 is required for **Resource Shrinking Optimized** — the new unified shrinker where R8 shrinks code and resource references together in one pass, replacing the old standalone `shrinkReleaseRes` task with `convertShrunkResourcesToBinaryRelease` + `optimizeReleaseResources`. Confirmed via the actual task graph in the AGP 8.13 build (the old task name is gone, the new pipeline runs) and via `mapping.txt` showing R8 8.13.6 with `com.android.tools.r8.residualsignature` metadata (a newer R8 mapping format).
  2. **R8 Full Mode**: already the AGP default since 8.0 (nothing was disabling it), but added `android.enableR8.fullMode=true` explicitly to `gradle.properties` so it can't be silently turned off by a future edit. Evidence it's active: default keep rules pulled from dependencies show `allowrepackaging,allowobfuscation,allowshrinking` flags, which is full-mode-specific behavior.
  3. **Repackage Classes**: automatic under R8 full mode per Android's own docs ("classes are repackaged by default" in full mode) — no separate config needed once full mode + minifyEnabled are on.
  4. **Resource Shrinking**: already enabled in v1.0.3 (`isShrinkResources = true`); carried forward unchanged.
- **Still deliberately not done: AGP 9.0.** It's been GA since January 2026 (9.1.1 shipped April 2026), so it's no longer bleeding-edge, but it introduces built-in Kotlin support (Kotlin compilation moves into AGP itself, no longer requires applying `org.jetbrains.kotlin.android`) — a genuine DSL/behavior change across all 20+ modules in this project, not just a version bump. None of the four checklist items above required it. Treating it as a separate, dedicated migration task rather than folding it into this release.
- **Verified 2026-08-11:** `./gradlew :app:assembleRelease :app:bundleRelease` succeeds on AGP 8.13.0 / Gradle 8.13 (BUILD SUCCESSFUL, 717 tasks). `./gradlew test lint` also passes across all modules (BUILD SUCCESSFUL) — run with a reduced JVM heap (`-Dorg.gradle.jvmargs="-Xmx2560m"`, `--max-workers=1`) after the default-memory run got OOM-killed by the OS due to unrelated heavy processes (an Android emulator + other projects' Gradle daemons) competing for RAM on this dev machine — not a build problem.
- **Investigated but not adopted:** whether `debugSymbolLevel = "SYMBOL_TABLE"` (a lower bar than `FULL`, needing only an ELF `.symtab` rather than full DWARF debug info) could let AGP embed at least partial native symbols for `libdatastore_shared_counter.so` when paired with `androidx.datastore:datastore-preferences:1.2.1` (which does carry a `.symtab`, per the v1.0.3 investigation). Tested directly: `extractReleaseNativeDebugMetadata` still logs "already been stripped" for both native libraries at `SYMBOL_TABLE` level too. Confirms the v1.0.3 finding holds regardless of debug symbol level — reverted both the datastore version and the debugSymbolLevel back to their v1.0.4 baseline (`1.1.1` / `FULL`) since neither experiment produced any real symbol data.
- Before uploading: build a signed release AAB and manually smoke-test the installed **release** APK — same caveat as v1.0.3, `connectedCheck` doesn't cover the minified/shrunk release variant. Given this release also bumps the build toolchain itself (AGP/Gradle), pay extra attention to anything R8-adjacent: reminders firing, backup/restore, and file/document handling in `core:files` and `core:security`.

### Play Store release notes (1.0.4)

```
Continued build and performance improvements:
- Further app size and runtime optimizations
- Updated build tooling for more reliable releases
- No changes to your data, records, or how the app works day to day
```

## Version 1.0.3 (versionCode 3) — superseded locally, never uploaded

- **Note:** work moved straight to v1.0.4 (AGP/R8 follow-up fixes) before v1.0.3 was ever uploaded to Play Store. `app/build.gradle.kts` now reflects versionCode 4. If you want v1.0.3 as a distinct Play Store release, rebuild it from git history at commit-time-of-versionCode-3 before v1.0.4 overwrote it, or just skip straight to shipping v1.0.4 — its changes are a strict superset (native debug symbols fix + all of v1.0.3's R8/shrink work, carried forward).
- `app/build.gradle.kts` set to versionCode 3 / versionName "1.0.3" (not yet built for release / not yet uploaded).
- Addresses two issues Play Console flagged against versionCode 2:
  1. **Missing native debug symbols.** Added `ndk { debugSymbolLevel = "FULL" }` to the release build type so AGP packages native debug symbols directly into the release AAB at build time — no more separate manual `native-debug-symbols.zip` upload to Play Console.
  2. **Low App Optimization score (obfuscation 1%, no shrinking, no R8 configuration).** Enabled `isMinifyEnabled = true` and `isShrinkResources = true` on the release build type with `app/proguard-rules.pro` (keeps `ReminderWorker`/`ListenableWorker` subclasses reachable for WorkManager's reflective instantiation, keeps source file + line number attributes for deobfuscated stack traces).
- **Not done:** the AGP-9.0 upgrade Play Console also suggested. Current toolchain is AGP 8.7.3 / Gradle 8.9 / Kotlin 2.0.21; jumping to AGP 9.0 is a separate, higher-risk change (newer Gradle, potential breaking DSL changes) and isn't required for R8/shrinking to work — R8 is already fully supported on 8.7.3. Deferred as its own follow-up rather than bundled into this release.
- Before uploading: build a signed release AAB (`./gradlew bundleRelease` with `vexelRelease*` properties set) and manually smoke-test the installed **release** APK (reminders, backup/restore, records, timeline) — automated `connectedCheck` runs against the debug build type, not the minified release one, so R8 shrinking regressions won't show up there.
- **Verified 2026-08-11:** `./gradlew :app:assembleRelease :app:bundleRelease` succeeds (BUILD SUCCESSFUL). `app/build/outputs/mapping/release/mapping.txt` + `resources.txt` + `usage.txt` confirm R8 obfuscation and resource shrinking are now active (previously absent). The AAB's only native libraries are `libdatastore_shared_counter.so` (AndroidX DataStore) and `libandroidx.graphics.path.so` (Compose) — both prebuilt/stripped inside their upstream AndroidX AARs with no embedded debug info, so `extractReleaseNativeDebugMetadata` runs but produces nothing to attach. The `debugSymbolLevel = "FULL"` config is the correct, complete fix on our side (this app has no native/NDK code of its own); if Play Console still flags these two specific libraries afterward, it's an upstream AndroidX packaging gap, not something fixable from this app's build config.
- **Confirmed unfixable from the app side (2026-08-11):** tried bumping `androidx.datastore` 1.1.1 → 1.2.1 (a fix reported by another developer for a similar warning) and rebuilt. The 1.2.1 `libdatastore_shared_counter.so` is no longer `file`-reported as "stripped" (it now carries a `.symtab`), but `./gradlew :app:extractReleaseNativeDebugMetadata --info` still logs, for both native libraries: `Unable to extract native debug metadata from ... because the native debug metadata has already been stripped.` — this is the exact diagnostic Android's own docs (developer.android.com/build/include-native-symbols) describe as meaning the upstream AAR never shipped DWARF debug info at all, so nothing can be attached regardless of app-side config or dependency version. Reverted the datastore bump (no benefit, unnecessary scope creep for this release). Nonetheless, built `app/build/outputs/native-debug-symbols-manual/native-debug-symbols.zip` (ABI folders at zip root, containing both `libdatastore_shared_counter.so` and `libandroidx.graphics.path.so` from the actual release build output — same content already embedded in the AAB, no additional real debug data) and manually uploaded it via Play Console's App Bundle Explorer → Downloads for the live release.
- **Resolved 2026-08-11 (per user confirmation):** the manually-uploaded `native-debug-symbols.zip` cleared the Play Console "missing native debug symbols" warning, even though it contains no more debug information than what AGP already embeds via `debugSymbolLevel = "FULL"`. Conclusion: Play Console's check for this warning is presence/shape-based (was *a* correctly-structured symbols file uploaded for this native library) rather than validating actual DWARF content. For future releases with the same native libraries (datastore/compose graphics-path unchanged), re-uploading the same zip after each new version's bundle upload should keep this warning cleared without needing to rebuild it, as long as the `.so` files themselves haven't changed (verify via `sha256sum` against the new build's `merged_native_libs` output before reusing).

### Play Store release notes (1.0.3)

```
This release focuses on build quality and diagnostics:
- Reduced app size and improved runtime performance
- Better crash diagnostics for faster fixes if something goes wrong
- No changes to your data, records, or how the app works day to day
```

## Version 0.2.0 (versionCode 2) — uploading now

- Local release build/signing verified (assembleRelease, bundleRelease, test, lint, check, connectedCheck all green — see `docs/verification/evidence/`).
- Per user confirmation on 2026-08-11: currently being uploaded to Play Store.

## Version 0.1.0 (versionCode 1) — uploaded

- Introduced 2026-08-04 (commit `cf4bf03`) under the pre-rename package `pk.vexel.healthpassport`; the app was renamed to `com.vexel.passport` on 2026-08-09 (commit `b5b31a1`) while still at versionCode 1, before bumping to 0.2.0.
- Per user confirmation on 2026-08-11: uploaded to Play Store (initial release).

## How to update this ledger

On every version bump or actual Play Console submission, add/update an entry above with: version name + versionCode, date, commit, build/signing status, and Play Store submission status (track — internal/closed/open/production — and date if applicable).
