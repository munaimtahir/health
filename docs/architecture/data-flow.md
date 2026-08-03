# Data Flow

Status: Canonical. Purpose: describe offline-first state flow.

Compose sends user intents to a ViewModel. The ViewModel invokes a domain use case. Repositories write to Room/DataStore or private file abstractions and expose `Flow` state. The UI renders immutable state, including loading, empty, and safe error states. Future sync is optional and cannot replace local truth.

