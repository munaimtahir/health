# Defect Register

| Defect ID | Severity | Sprint | Area | Description | Reproduction | Expected | Actual | Root cause | Fix | Regression test | Status |
|---|---:|---:|---|---|---|---|---|---|---|---|---|
| DEF-001 | 0 | 0 | Build | KSP target differed from Java target on host JDK 21 | Run initial Gradle gate | Build proceeds with Java 17 targets | `kspDebugKotlin` failed with JVM-target mismatch | Root build did not configure Kotlin target | Root `build.gradle.kts` now applies JVM 17 to Kotlin compile tasks | Full Gradle test/build rerun pending | FIXED |
| DEF-002 | 3 | 0 | Emulator | Emulator launch displayed System UI not-responding dialog during smoke launch | Install/launch on `emulator-5554` | App launch can be inspected | Dialog appeared; ActivityManager still showed app resumed | Environmental System UI condition not reproduced on physical target | Repeat emulator after environment stabilizes | OPEN / ENVIRONMENTAL |
