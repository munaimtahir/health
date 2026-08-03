# Navigation Architecture

Status: Canonical. Purpose: define route ownership.

The root app owns the NavHost and stable top-level destinations. Features expose composable entry points and route contracts. Routes use stable IDs and avoid passing sensitive records through serialized navigation arguments; repositories load data locally.

