# Permissions Register

Status: Canonical. Purpose: track runtime and manifest permissions.

| Permission | Feature | Required? | Refusal behavior |
|---|---|---|---|
| None in scaffold | Shell | No | App remains fully usable. |
| Notifications | Future reminders | Optional | Reminders remain visible in-app; no notification is posted. |
| Camera | Future scanning | Optional | User can import through SAF instead. |
| Storage/media access | Future imports, only if platform requires | Optional | SAF picker remains the fallback. |

