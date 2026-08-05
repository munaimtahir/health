# Accessibility verification

## Implemented checks

- Primary navigation has visible text labels and icon content descriptions.
- Empty and loading components expose readable text; loading progress has a content description.
- Status pills include text and are not color-only indicators.
- Forms and dialogs use scrollable content so larger text can remain reachable.
- Primary actions use full-width or comfortably padded controls where appropriate.
- Error and confirmation content remains textual and actionable.

## Evidence and remaining manual checks

Physical-device visual evidence is in `docs/verification/evidence/`. The physical large-font capture demonstrates readable onboarding at 130% system font scale. A dedicated TalkBack traversal, 200% font-scale, contrast, and landscape pass remains part of the final gate; it must not be inferred from compilation alone.
