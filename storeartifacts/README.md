# Vexel Health Passport store artifacts

This directory contains the working material for the initial Google Play listing for **Vexel Health Passport** (`com.vexel.passport`). It reflects release `0.1.0` (version code `1`) and the current offline-first implementation.

## Contents

- `store-listing.md` — app title, short description, full description, category, and contact-field guidance.
- `release-notes.md` — initial release notes for the release track.
- `privacy-policy.md` — comprehensive publication draft covering local health-data handling.
- `data-safety-declaration.md` — proposed answers for Play Console Data safety.
- `health-apps-declaration.md` — proposed health feature selections and required disclaimer.
- `developer-account-requirement.md` — determination that an organization account and D-U-N-S number are required.
- `permissions-declaration.md` — manifest permission and platform-access explanation.
- `content-rating-and-audience.md` — proposed content-rating and audience answers.
- `app-access-and-review-notes.md` — instructions for the Play review team.
- `submission-checklist.md` — owner-facing upload checklist.
- `owner-input-required.md` — values that cannot be inferred from the repository.
- `graphics/` — feature graphic, generation prompt, dimensions, and alt text.

## Publication status

The technical copy is grounded in the current source and manifest. Replace every bracketed owner placeholder before publication and have the privacy policy reviewed for the developer's jurisdiction. The privacy policy must be hosted at a public, non-geofenced, non-editable webpage URL and must also be accessible from inside the app.

The proposed Data safety answer is **no data collected or shared** because the app has no network permission or network client and Google Play defines collection as transmission off the device. User-directed export, backup, and Android sharing are described separately because the developer does not receive those files.

## Policy references checked

- Google Play preview asset requirements: https://support.google.com/googleplay/android-developer/answer/9866151
- Google Play Data safety guidance: https://support.google.com/googleplay/android-developer/answer/10787469
- Google Play User Data policy: https://support.google.com/googleplay/android-developer/answer/10144311
- Google Play Health apps declaration: https://support.google.com/googleplay/android-developer/answer/14738291
- Google Play Health Content and Services policy: https://support.google.com/googleplay/android-developer/answer/16679511
- Google Play developer account type guidance: https://support.google.com/googleplay/android-developer/answer/13634885
- Google Play Console requirements: https://support.google.com/googleplay/android-developer/answer/10788890
- Google Play organization account information: https://support.google.com/googleplay/android-developer/answer/13628312

Policy guidance was rechecked on 2026-08-07. Recheck it in Play Console immediately before submission because form wording may change.
