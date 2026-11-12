#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
checks = {
    "Android manifest": ROOT / "app/src/main/AndroidManifest.xml",
    "Camera activity": ROOT / "app/src/main/java/com/gestra/camera/CameraActivity.kt",
    "Hand analyzer": ROOT / "app/src/main/java/com/gestra/camera/HandGestureAnalyzer.kt",
    "Settings screen": ROOT / "app/src/main/java/com/gestra/camera/SettingsActivity.kt",
    "Roadmap": ROOT / "app/src/main/java/com/gestra/camera/RoadmapActivity.kt",
    "Workflow": ROOT / ".github/workflows/android-build.yml",
    "Model download script": ROOT / "scripts/download-model.sh",
}
missing = [name for name, path in checks.items() if not path.is_file()]
if missing:
    print("Missing required project files:", ", ".join(missing), file=sys.stderr)
    raise SystemExit(1)

camera = (ROOT / "app/src/main/java/com/gestra/camera/CameraActivity.kt").read_text(encoding="utf-8")
analyzer = (ROOT / "app/src/main/java/com/gestra/camera/HandGestureAnalyzer.kt").read_text(encoding="utf-8")
manifest = (ROOT / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
roadmap = (ROOT / "app/src/main/java/com/gestra/camera/RoadmapActivity.kt").read_text(encoding="utf-8")

required_tokens = [
    "ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY",
    "MediaStore.Images.Media.EXTERNAL_CONTENT_URI",
    "RenderEffect.createBlurEffect",
    "cameraControl.enableTorch",
    "getLaunchIntentForPackage",
    "HandGesture.OPEN_PALM",
    "HandGesture.THUMBS_UP",
    "HandGesture.SHAKA",
    "HandGesture.PEACE",
]
combined = camera + analyzer
missing_tokens = [token for token in required_tokens if token not in combined]
if missing_tokens:
    print("Missing implementation markers:", ", ".join(missing_tokens), file=sys.stderr)
    raise SystemExit(1)

if 'android.permission.CAMERA' not in manifest:
    raise SystemExit("Camera permission is missing from manifest")
if 'android:name="android.permission.READ_EXTERNAL_STORAGE"' in manifest:
    raise SystemExit("Unexpected broad legacy storage permission")

idea_count = len(re.findall(r'FeatureIdea\("', roadmap))
if idea_count < 60:
    raise SystemExit(f"Expected at least 60 feature ideas, found {idea_count}")

print(f"Static checks passed. Project files: {len(checks)}; feature catalog: {idea_count} ideas.")
