# Proposed Google Play Data safety declaration

This is a submission worksheet, not a substitute for completing the form in Play Console. Re-audit the exact final AAB and every bundled SDK before submission.

## Recommended top-level answers

- Does the app collect or share any of the required user data types? **No**
- Is user data shared with other companies or organizations by the developer? **No**
- Does the app provide account creation? **No**
- Does the app include an in-app data deletion mechanism? **Yes — Settings > Delete all app data**

## Basis

Google Play defines collection as transmitting data off the user's device. The current app has no `INTERNET` permission, network client, analytics SDK, advertising SDK, crash-reporting SDK, account service, cloud sync, or developer server. Personal and health information is stored locally in app-private storage.

The user can deliberately save or share exports, reports, documents, and encrypted backups through Android system interfaces. The destination is selected by the user, the developer does not receive a copy, and the action is described in the privacy policy.

## Locally handled data

The following data may be accessed and stored locally but is not transmitted to the developer in the current release:

- Personal information entered in the profile.
- Health information, symptoms, allergies, diagnoses, procedures and notes.
- Medication and treatment information.
- Appointment and reminder information.
- User-selected documents, images, reports, exports and encrypted backups.
- App preferences and protected app-lock material.

## Security and deletion statements

- Core data is stored in Android app-private storage.
- Backups are encrypted with a user-entered password using password-derived AES-GCM encryption.
- User-directed sharing relies on Android temporary URI grants and the security of the chosen destination.
- Users can delete individual records and can delete all app-managed local data.
- Uninstalling or clearing app storage removes app-managed local data, subject to Android behavior.
- Files already exported or shared must be deleted from their destination separately.

## Final pre-submit audit

Change the top-level answer to **Yes** and disclose every applicable data type if any release adds network transmission, telemetry, crash reports, cloud backup, account services, remote support uploads, remote AI/OCR, advertising, or an SDK that sends user or device data off-device.
