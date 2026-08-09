# Deferred items

Engineering deferred items: none. All implementation, test, build, release-artifact, and connected-device gates have passed. The only remaining owner-controlled action is manual Play Console submission/publication of the signed AAB. Local release signing is complete and the signed APK/AAB are generated outside this deferred list.

A successful biometric-enrolled unlock, previously deferred because the connected TECNO has fingerprint hardware but no enrolled biometric, is now verified: a virtual fingerprint was enrolled on the API 26 emulator and `BiometricUnlockSuccessTest` passed against a real system `BiometricPrompt`. The app's unavailable/no-enrollment capability path remains separately verified on the TECNO.

One item remains genuinely open rather than deferred: reminder behavior specifically *during* forced Doze idle was inconclusive on this emulator image (see `DEFECT_REGISTER.md` DEF-005) — durable persistence and real reboot survival are verified, but live in-Doze firing timing is not conclusively proven or disproven in this environment.
