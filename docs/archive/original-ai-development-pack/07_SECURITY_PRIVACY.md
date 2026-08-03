# Security and Privacy Requirements

## Privacy principles
- Collect only data required for user-selected functionality.
- Core use requires no account.
- No advertising SDK.
- No sale of health information.
- No unrelated background collection.
- No sensitive data in logs, analytics or crash breadcrumbs.
- User can export and permanently delete data.

## Local protection
- Use app-private storage.
- Protect cryptographic keys with Android Keystore.
- Encrypt sensitive database/file content using a maintained, reviewed approach.
- Offer biometric/PIN application lock.
- Lock after configurable inactivity.
- Prevent sensitive previews in recent-apps view where practical.

## File protection
- Preserve source files.
- Validate imported file type and size.
- Store opaque internal names.
- Check checksum.
- Never expose unrestricted file-provider paths.
- Grant temporary sharing permissions only.

## Backup and restore
- Backup must be opt-in.
- Backup archive encrypted.
- Backup manifest includes schema/app version and checksums.
- Restore verifies integrity before replacing data.
- Maintain rollback or failure-safe behavior.
- Display last successful backup time.

## AI and OCR safeguards
Future AI functions must:
- Be optional.
- Clearly state whether processing is on-device or external.
- Require explicit consent before external processing.
- Minimize transmitted content.
- Never overwrite the original report.
- Present extracted fields as suggestions.
- Require user confirmation before adding structured health data.
- Avoid diagnosis and treatment advice.

## Medical disclaimer
Suggested wording:
“This application helps users record and organize symptoms, treatments and personal health information. It does not provide medical advice, diagnosis, treatment recommendations or emergency assessment. Seek qualified medical care for health concerns and urgent assistance for emergencies.”

## Incident priorities
1. Prevent further exposure or corruption.
2. Preserve evidence without collecting additional sensitive data.
3. Inform affected users clearly where required.
4. Patch and test.
5. Review architecture and process root cause.
