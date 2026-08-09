#!/usr/bin/env bash
set -euo pipefail
./gradlew assembleDebug
./gradlew test
./gradlew lint
./scripts/verify_boundaries.sh
