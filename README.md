# LabDroid

LabDroid is a free, ad-free Android app that reads and displays live data from your device's sensors, hardware, location, camera, and connectivity — entirely on-device, with no accounts, no cloud, and no analytics.

- Package: `com.labdroid.app`
- Minimum Android version: 8.0 Oreo (API 26), tested up to Android 16 (API 36)
- Author: Tomasz Pieczara ([spotrobotics.app](https://spotrobotics.app))

## Features

- **Dashboard** — device model, manufacturer, Android version, kernel, build number, CPU architecture, RAM, storage, battery, GPS, sensors, cameras and connectivity at a glance.
- **Sensors** — full list of available hardware sensors with live readings, per-sensor detail screens (live chart, min/max/average/std. deviation, metadata: vendor, resolution, range, power, reporting mode).
- **Hardware** — deep dive into CPU, GPU, memory, storage, display, flashlight, audio and connectivity (Wi-Fi, cellular, Bluetooth, NFC).
- **Monitor** — pin multiple sensors, record sessions, and export them as CSV, JSON, XML, TXT or GPX.
- **GPS** — live location and compass with true-north correction, satellite signal radar, track recording and GPX/CSV export.
- **Cameras** — per-camera sensor, resolution, focal length, FoV, zoom, OIS, flash and RAW capability.
- **Battery** — level, health, charging source, voltage, current, temperature, cycle count and other electrical parameters.
- **Search** — quick access to any sensor, hardware section or setting.
- **Settings** — light/dark theme, metric/imperial units, default sensor sampling speed, permission overview, export folder, and language (English/Polish).

## Privacy

LabDroid reads sensor, location, camera and hardware data exclusively on your device to display it in the app or let you export it yourself. Nothing leaves the device automatically — there are no remote servers, analytics SDKs, ad networks or third parties involved.

## Tech stack

- Kotlin, Jetpack Compose (Material 3)
- Hilt (dependency injection)
- Room (local recording storage)
- DataStore (preferences)
- Jetpack Navigation Compose

## Building

Requirements: JDK 17, Android SDK (compileSdk/targetSdk 36).

```bash
./gradlew assembleDebug
```

The debug build task automatically copies the produced APK to the project's parent directory and bumps the patch version in `app/version.properties`.

## Contact

tomasz.pieczara@gazeta.pl
