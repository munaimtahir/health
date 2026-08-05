# Vexel design system

## Implementation

The controlled theme and shared states live in:

- `core/designsystem/src/main/kotlin/pk/vexel/healthpassport/core/designsystem/Theme.kt`
- `core/designsystem/src/main/kotlin/pk/vexel/healthpassport/core/designsystem/Components.kt`

## Tokens

The light palette uses teal primary actions (`#0F766E`), slate secondary content, an `#F8FAFC` background, white cards, and explicit semantic colors. The dark palette uses navy surfaces rather than pure black. Typography uses Material 3 roles with readable body line height and stronger heading hierarchy.

Shared components include `InformationCard`, `SectionHeader`, `EmptyState`, `LoadingState`, and `StatusPill`. Screen-level cards and navigation explicitly use theme surface colors to avoid uncontrolled tonal variation.

## Rules

- 16dp standard horizontal screen padding; 8/12/16/20/24dp spacing as appropriate.
- 48dp minimum target intent for interactive controls.
- Text labels accompany primary navigation icons.
- Statuses include text, not color alone.
- Destructive actions remain separate from routine actions and require confirmation.
- Long forms scroll and preserve entered state during validation.
