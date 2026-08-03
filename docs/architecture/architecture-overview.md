# Architecture Overview

Status: Canonical. Purpose: define the implementation baseline.

Vexel Health Passport uses Kotlin, Compose, Material 3, MVVM with unidirectional state flow, Hilt, Room, DataStore, Navigation Compose, WorkManager, Keystore abstractions, SAF, and JUnit/AndroidX tests. Room is the local source of truth; domain contracts remain platform-light; UI observes flows and reports success only after local writes commit. Minimum SDK is 26 and target SDK is 36 for the installed toolchain.

