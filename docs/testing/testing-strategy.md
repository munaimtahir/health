# Testing Strategy

Status: Canonical. Purpose: define scaffold and future test coverage.

Use JUnit for domain and repository logic, AndroidX Test for device behavior, Compose UI tests for screens/navigation, Room tests for opening and migrations, and DataStore tests for preferences. Use deterministic clocks and test dispatchers. Never use real patient data. The shell gate covers launch, dashboard, navigation, themes, Room, DataStore, and offline operation.

