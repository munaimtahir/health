# Google Play submission checklist

## Owner values

- [ ] Obtain a D-U-N-S number and verify a Google Play organization developer account.
- [ ] Confirm the organization name, address, phone number and website match authoritative records.
- [ ] Replace every bracketed placeholder in `privacy-policy.md` and `store-listing.md`.
- [ ] Confirm the legal developer name exactly matches the Play Console listing.
- [ ] Add a monitored support email and support website.
- [ ] Confirm adult target audience and distribution countries.
- [ ] Obtain jurisdiction-appropriate legal review.

## Privacy and policy

- [ ] Host the finalized privacy policy on a public HTTPS webpage that is non-geofenced, non-editable and not a PDF.
- [ ] Add the privacy policy URL or policy text inside the app.
- [ ] Complete Data safety using the final AAB and SDK inventory.
- [ ] Complete Health apps declaration with all applicable features.
- [ ] Confirm the app is not regulated as a medical device in any target jurisdiction.
- [ ] Keep the non-medical-device disclaimer in the full description and in-app disclosure.
- [ ] Complete content rating, target audience, app access and ads declarations.

## Store listing and graphics

- [ ] Enter the app name, short description and full description.
- [ ] Upload the 512 by 512 app icon.
- [ ] Upload `graphics/feature-graphic.png` at 1024 by 500 pixels with no alpha.
- [ ] Add the feature graphic alt text from `graphics/README.md`.
- [ ] Capture and upload current phone screenshots using synthetic information only.
- [ ] Review all images for real names, dates of birth, symptoms, medications, diagnoses, filenames, paths and device notifications.

## Release artifact

- [ ] Create the listing in the verified organization account, or complete an eligible transfer from the personal account.
- [ ] Create a new Play Console app for package `com.vexel.passport`.
- [ ] Configure Google Play App Signing and securely back up the signing key.
- [ ] Confirm `versionCode = 1` is unused for this new package.
- [ ] Re-run clean release build, tests, lint and connected checks against the exact release commit.
- [ ] Verify AAB signature and package identity.
- [ ] Upload `app/build/outputs/bundle/release/app-release.aab`.
- [ ] Start with internal or closed testing before production rollout.
