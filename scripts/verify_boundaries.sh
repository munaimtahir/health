#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

fail_if_found() {
  local message="$1"
  shift
  if grep -rEn --exclude-dir=build "$@"; then
    echo "$message" >&2
    exit 1
  fi
}

fail_if_found \
  "Core app manifests must not request network access." \
  --include='AndroidManifest.xml' \
  'android\.permission\.(INTERNET|ACCESS_NETWORK_STATE)' \
  app/src/main core feature

fail_if_found \
  "Network or analytics dependencies require an approved decision record." \
  --include='*.gradle' --include='*.gradle.kts' --include='*.toml' \
  '(firebase-analytics|firebase-crashlytics|amplitude|mixpanel|segment-analytics|appcenter-analytics|okhttp|retrofit|ktor-client|volley)' \
  .

fail_if_found \
  "Core application source must not introduce direct network clients." \
  --include='*.kt' --include='*.java' \
  '(java\.net\.(URL|URI|HttpURLConnection)|android\.net\.http|org\.apache\.http|https?://)' \
  app/src/main core/*/src/main feature/*/src/main

fail_if_found \
  "User-facing source crossed the non-diagnostic safety boundary." \
  -i --include='*.kt' --include='*.xml' \
  '(caused by|you should take|we recommend|start taking|stop taking|diagnosis is|you likely have|seek emergency care|follow[- ]?up in [0-9])' \
  app/src/main core/*/src/main feature/*/src/main

grep -q 'android:exported="false"' app/src/main/AndroidManifest.xml
grep -q 'android:grantUriPermissions="true"' app/src/main/AndroidManifest.xml
grep -q 'android:allowBackup="false"' app/src/main/AndroidManifest.xml

echo "Privacy, offline, sharing-provider, and medical-safety boundary checks passed."
