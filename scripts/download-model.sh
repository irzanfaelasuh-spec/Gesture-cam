#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ASSET_DIR="$ROOT_DIR/app/src/main/assets"
MODEL_PATH="$ASSET_DIR/hand_landmarker.task"
MODEL_URL="https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/1/hand_landmarker.task"

mkdir -p "$ASSET_DIR"
if [[ -s "$MODEL_PATH" ]] && [[ "$(wc -c < "$MODEL_PATH")" -gt 1000000 ]]; then
  echo "MediaPipe model already present: $MODEL_PATH"
  exit 0
fi

command -v curl >/dev/null 2>&1 || { echo "curl is required to download the MediaPipe model." >&2; exit 1; }
TEMP_PATH="$MODEL_PATH.download"
rm -f "$TEMP_PATH"
curl --fail --location --retry 3 --retry-delay 2 \
  --connect-timeout 30 --max-time 180 \
  --user-agent "GESTRA-Android-Build/1.0" \
  "$MODEL_URL" -o "$TEMP_PATH"

SIZE="$(wc -c < "$TEMP_PATH")"
if [[ "$SIZE" -lt 1000000 ]]; then
  echo "Downloaded model is unexpectedly small ($SIZE bytes)." >&2
  rm -f "$TEMP_PATH"
  exit 1
fi
mv "$TEMP_PATH" "$MODEL_PATH"
echo "MediaPipe hand model ready ($SIZE bytes)."
