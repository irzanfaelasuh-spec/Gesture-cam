# GESTRA — Gesture Camera System

**Status:** DEMO TESTING · v0.1.0  
**Platform:** Android 12+ (API 31+)  
**Package:** `com.gestra.camera`  
**Build:** Kotlin · CameraX · MediaPipe Tasks Vision · GitHub Actions

GESTRA is an experimental Android camera app controlled by four hand poses. The hand detector runs on-device; no cloud AI key, account, or paid backend is used.

## Gesture actions

| Pose | Action | Notes |
|---|---|---|
| Peace / V sign | Blur the live camera preview | Blur stays active while the pose remains stable. The preview returns to normal after the pose is released. |
| Open palm | Capture a photo | CameraX captures a real JPEG and saves it under `Pictures/GESTRA` using Android MediaStore. |
| Thumbs up | Open a selected application | Choose an installed launcher app in Settings first. |
| Shaka | Turn on the LED flash | Requires a rear camera with a flash unit. Use the on-screen Flash control to turn it off. |

Actions trigger once per stable pose, not on every analysis frame. The detector requires several consecutive frames to reduce flicker and accidental triggers.

## First-run flow

1. Four-second animated GESTRA introduction with a short synthesized tone.
2. Permission checklist. Camera access is required. Shizuku is optional and is not required by this build. MediaStore handles photo saving without broad storage access. Notifications are not requested because this build does not run a background camera service.
3. A `3 / 2 / 1 / OPEN GESTRA` entry sequence.
4. Live camera preview with shutter, lens switch, flash control, and Settings.

The Settings screen includes the **DEMO TESTING** label, per-gesture switches, recognition sensitivity, stable frame count, blur strength, composition grid, shutter delay, sound and haptic controls, app selection, and a 67-item feature catalog. Catalog entries marked **PLANNED** are concepts for future work, not implemented controls.

## Build with GitHub Actions (recommended on a phone)

1. Create a GitHub repository and upload the contents of this ZIP to its root. Keep the `.github/workflows` folder and the `scripts` folder.
2. Open the repository's **Actions** tab.
3. Select **GESTRA Android Build** and press **Run workflow**, or push a commit to `main` / `master`.
4. When the workflow succeeds, open the run and download the artifact named **GESTRA-debug-apk**.
5. Install the APK on an Android 12+ device and grant Camera permission.

The workflow installs JDK 17, Gradle 8.13 and Android SDK 36, downloads the MediaPipe model, runs a static project check, and builds `app-debug.apk`.

## Build locally

A small `gradlew` bootstrap script is included for Termux/Linux. It downloads the pinned Gradle 8.13 distribution once, then runs it:

```bash
chmod +x gradlew scripts/download-model.sh scripts/static_check.py
./gradlew assembleDebug
```

The Android SDK (platform 36 and build tools 36.0.0), JDK 17, `curl` and `unzip` must be installed locally. The Gradle build downloads the hand model automatically when it is missing. You can also download it explicitly:

```bash
./scripts/download-model.sh
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Important implementation notes

- **Shizuku:** deliberately not integrated. Camera access, photo capture, flash control, and opening a user-selected app are handled with standard Android APIs. Shizuku would not make the hand detector faster.
- **Privacy:** camera frames are processed locally by MediaPipe. The app has no Internet permission and no API key. Gradle downloads the model during the build, not during normal app use.
- **Photo saving:** the app uses `ImageCapture` and MediaStore. It does not request broad file/storage permissions.
- **Blur:** uses Android `RenderEffect`, which is available from API 31. This is why the current minimum Android version is 12.
- **HD:** the preview requests a 1920×1080 16:9 stream and the still capture uses CameraX quality-priority mode. Actual resolution depends on the camera hardware and CameraX's supported stream combinations; the app cannot force a sensor to produce a resolution it does not support.
- **Real-time scope:** gesture recognition runs while GESTRA's camera screen is active and visible. This demo does not try to keep the camera secretly running in the background.
- **Pose accuracy:** lighting, hand distance, camera angle, and device performance affect recognition. The first release needs testing on real devices. A pose classifier based on landmarks is not guaranteed to recognize every hand orientation.
- **Torch:** the Shaka action turns the torch on. The on-screen Flash control turns it off or on manually. The torch may be unavailable on front cameras or devices without an LED flash.

## Current limitations

- No background gesture detection after leaving the camera screen.
- Gesture recognition is tuned for a hand held reasonably upright and visible in frame.
- The app picker lists apps that expose a launcher activity.
- Some camera devices may fall back to a lower preview resolution or may not support flash.
- This source package has not been device-tested on every Android manufacturer. The first GitHub Actions run is the authoritative compile check for the repository it builds.

## Technology references

- CameraX: https://developer.android.com/jetpack/androidx/releases/camera
- CameraX image capture: https://developer.android.com/media/camera/camerax/take-photo
- MediaPipe Hand Landmarker for Android: https://developers.google.com/edge/mediapipe/solutions/vision/hand_landmarker/android
- Android RenderEffect: https://developer.android.com/develop/ui/views/graphics/renderscript/migrate
- Android Gradle Plugin compatibility: https://developer.android.com/build/releases/about-agp

## Icon

The launcher icon is vector XML under `app/src/main/res/drawable/` and the adaptive icon descriptor is under `app/src/main/res/mipmap-anydpi-v26/`. No full-resolution PNG icon is included.
