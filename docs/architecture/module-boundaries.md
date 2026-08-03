# Module Boundaries

Status: Canonical. Purpose: prevent coupling and circular dependencies.

`app` composes features and owns navigation. Features depend only on approved core modules and never on each other. `core:model` contains pure shared models; `core:domain` contains platform-light contracts and use cases; `core:database` owns Room; `core:security` owns sensitive-storage abstractions; `core:files` owns file contracts; `core:testing` owns fakes. No core module depends on `app` or presentation.

