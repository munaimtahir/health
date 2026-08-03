# Quality Gates

Status: Canonical. Purpose: define evidence required for delivery.

Scaffold gates: debug build, unit tests, lint, app identity, navigation/UI smoke tests, Room/DataStore checks, no network requirement, no feature coupling, no secrets, no analytics/ads, and documented medical boundary. Device tests and `connectedCheck` run when an emulator/device is available; blocked environment results must be reported, never called passed.

