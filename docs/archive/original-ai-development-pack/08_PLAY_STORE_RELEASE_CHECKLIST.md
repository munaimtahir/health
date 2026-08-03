# Play Store and Release Checklist

## Technical
- Target API 36
- Signed release AAB
- Version code and version name set
- R8/proguard reviewed
- Native debug symbols uploaded if applicable
- No debug endpoints or test credentials
- Backup and restore tested
- Database migration tested

## Health-app policy
- Health Apps declaration completed accurately
- Data Safety form matches actual behavior
- Privacy policy publicly accessible
- Medical disclaimer included in app and listing
- Sensitive permission disclosures clear
- No diagnostic or treatment claims
- No unsupported claims of encryption, anonymity or regulatory approval

## Store listing
Working title:
Vexel Health Passport: Records

Short description:
Private health records, symptom history, medicines and follow-up reminders.

Core listing messages:
- One private health timeline
- Store reports and prescriptions
- Track symptoms and medication changes
- Remember follow-ups and check-ups
- Prepare a concise appointment summary
- Works offline

## Required store assets
- App icon
- Feature graphic
- Phone screenshots
- Privacy-policy URL
- Support email
- Support page

## Closed testing
- Recruit users with varied ages and Android experience.
- Include chronic-condition users and healthy users maintaining records.
- Collect structured feedback on trust, ease, report usefulness and reminder reliability.
- Log data-loss and privacy defects as release blockers.

## Release decision
Release only when:
- No critical/high defects
- Privacy and policy review complete
- At least one clean-install backup/restore test passes on another device or emulator
- Appointment report manually verified against source records
- Reminder tests pass across reboot and time-zone change
