# Reminder Architecture

Status: Canonical. Purpose: define future reminder scheduling.

Reminder definitions live in Room. WorkManager schedules unique work and reconciles after reboot, app update, and time-zone changes. Notification denial is a supported state. Every reminder is presented as a user-entered plan, not an app prescription.

