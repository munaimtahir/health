# Permissions and platform access declaration

## Manifest permission

### `android.permission.POST_NOTIFICATIONS`

Purpose: display medication, appointment, follow-up and custom reminders created by the user.

Behavior:

- Requested only on Android versions that require runtime notification permission.
- Reminder notifications contain privacy-minimized wording.
- The user can deny or revoke the permission in Android settings.
- Denial does not prevent local health-record organization; it only prevents notifications.

## User-directed file access

Imports, exports, reports and backups use Android system document pickers. The app does not request broad storage access. The user chooses each source or destination.

Private documents are exposed to another app only after a user-initiated open or share action through a non-exported `FileProvider` with temporary URI permission.

## Device authentication

Optional biometric or device-credential authentication uses Android's system authentication interface. The app does not receive or store biometric templates.

## Permissions not requested

The current manifest does not request internet, location, contacts, camera, microphone, body sensors, Health Connect, phone, SMS, calendar or broad external-storage permissions.
