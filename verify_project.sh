#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
EVIDENCE_DIR="$ROOT_DIR/docs/verification/evidence"
mkdir -p "$EVIDENCE_DIR"

run_stage() {
  local name="$1"
  shift
  local log="$EVIDENCE_DIR/${name}.log"
  echo "== $name =="
  "$@" 2>&1 | tee "$log"
}

cd "$ROOT_DIR"
run_stage clean ./gradlew clean
run_stage assemble-debug ./gradlew assembleDebug
run_stage assemble-release ./gradlew assembleRelease
run_stage bundle-release ./gradlew bundleRelease
run_stage unit-tests ./gradlew test
run_stage lint ./gradlew lint
run_stage check ./gradlew check
run_stage repository-verify ./scripts/verify.sh

if adb devices 2>/dev/null | awk 'NR > 1 && $2 == "device" { count++ } END { exit count == 0 }'; then
  run_stage connected-check ./gradlew connectedCheck
else
  echo "No authorized ADB device; connectedCheck was not run." | tee "$EVIDENCE_DIR/connected-check.log"
fi

echo "Verification stages completed. Logs: $EVIDENCE_DIR"
