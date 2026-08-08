# Owner input required

Complete these values before publishing or hosting any store artifact.

## Identity and contact

- `[LEGAL ORGANIZATION NAME]` — the verified entity that will own the Google Play organization account.
- `[D-U-N-S NUMBER]` — must identify the same legal organization.
- `[ORGANIZATION ADDRESS]`, `[ORGANIZATION PHONE]`, and `[ORGANIZATION WEBSITE]` — must match authoritative records.
- `[LEGAL DEVELOPER NAME]` — must match the developer or company shown on Google Play.
- `[SUPPORT EMAIL]` — monitored address for privacy and product questions.
- `[SUPPORT WEBSITE URL]` — public support page, if available.
- `[PRIVACY POLICY URL]` — public HTTPS webpage hosting the finalized privacy policy.
- `[POSTAL ADDRESS OR COUNTRY, IF LEGALLY REQUIRED]`.
- `[EFFECTIVE DATE]` and `[LAST UPDATED DATE]`.

## Policy decisions

- Confirm the intended audience. The current recommendation is adults aged 18 and over.
- Confirm that the app is not regulated as a medical device in any distribution country.
- Confirm distribution countries and the privacy-law review required for them.
- Confirm whether any SDK, permission, account, server, analytics, advertising, crash reporting, cloud sync, or support-upload behavior was added after this artifact pack was prepared.
- Confirm whether Play App Signing will use the current local key as the upload key or a separate upload key.

## Final evidence

- Create and verify the Google Play organization developer account before creating or transferring the Play listing.
- Host the privacy policy as a public non-PDF webpage.
- Add the same privacy policy link or text inside the app.
- Capture current screenshots using synthetic data only.
- Re-run release build, unit tests, lint, and connected checks against the exact commit to upload.
- Verify the final AAB package is `com.vexel.passport` and version code is unused in the new Play Console application.
