# Verification Blockers

## Owner-controlled manual action

Play Console upload, Google Play App Signing enrollment/selection, store declarations, and publication must be handled manually by the owner. This is not an engineering blocker for local signed-artifact generation or internal testing.

## Engineering gaps still open

The complete application acceptance pack is not complete: broader end-to-end/UI/accessibility automation and several product-level coverage items remain. These are engineering work, not owner action items. See `DEFERRED_ITEMS.md`.

Local release signing is now configured. A generated keystore is stored outside the repository at `/home/munaim/.config/vexel-health/vexel-health-release.jks`, with Gradle credentials stored in the machine-level Gradle properties using restrictive permissions. The keystore must be backed up securely and must never be committed.

The generated keystore must be backed up securely by the owner before any store publication. The keystore is the signing identity for this local release configuration and must never be committed.

The prior TECNO disconnect was resolved. The latest full verification run passed on physical serial `08357252AE006901`; retain the device connection for future runtime iterations.
