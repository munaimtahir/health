# Vexel design system

## Implementation

The controlled theme and shared states live in:

- `core/designsystem/src/main/kotlin/com/vexel/passport/core/designsystem/Theme.kt`
- `core/designsystem/src/main/kotlin/com/vexel/passport/core/designsystem/Components.kt`

## Tokens

The light palette uses teal primary actions (`#0F766E`), slate secondary content, an `#F8FAFC` background, white cards, and explicit semantic colors. The dark palette uses navy surfaces rather than pure black. Typography uses Material 3 roles with readable body line height and stronger heading hierarchy. Colors are defined once as named constants in `VexelColors` (not scattered inline hex literals) with a `contrastRatio()` utility; every body/action/error/status text-background pair is asserted at WCAG AA (≥4.5:1) by `core/designsystem/src/test/.../ColorContrastTest.kt`, which is part of `./gradlew test`. The light-theme `outline` color (used for every `OutlinedTextField` border) was corrected from `#CBD5E1` (1.48:1 against the white surface — a real accessibility failure below the 3:1 UI-component minimum) to `#64748B` (4.76:1).

Shared components include `InformationCard`, `SectionHeader`, `EmptyState`, `LoadingState`, `StatusPill`, and `ActionRow` (with an optional leading icon). Screen-level cards and navigation explicitly use theme surface colors to avoid uncontrolled tonal variation. Bottom-navigation labels use a font-scale-aware style: past 180% system font scale they shrink to an explicit small size with `maxLines = 1` to avoid truncation at 200% scale, verified visually in `docs/verification/evidence/accessibility-2026-08-09/`.

## Rules

- 16dp standard horizontal screen padding; 8/12/16/20/24dp spacing as appropriate.
- 48dp minimum target intent for interactive controls — asserted programmatically by `AccessibilityStructureTest` across all 5 tabs and every scrolled Profile section, not just documented.
- Text labels accompany primary navigation icons; Profile's action rows carry leading icons for scannability.
- Statuses include text, not color alone.
- Destructive actions remain separate from routine actions, require confirmation, and are styled with the theme's error color (not default primary) on the confirming button itself.
- Long forms scroll and preserve entered state during validation.
