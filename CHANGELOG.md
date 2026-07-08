# Changelog

## M8 — Rebrand to LabDroid, Locale/Battery/GPS-compass/Sound-level (2026-07-08)

- Renamed the project and package from TrueDroid (`com.truedroid.app`) to **LabDroid** (`com.labdroid.app`): app class, database, theme, navigation destinations/host, string resources (EN/PL), Gradle module and output APK name.
- Added English/Polish language switching (in-app locale picker).
- Added the **Battery** screen (level, health, charging source, voltage, current, temperature, cycle count).
- Added GPS **compass** with true-north correction and satellite signal radar to the GPS screen.
- Added a microphone-based **sound level** pseudo-sensor with its own detail screen.
- New app icon set and light/dark launcher/theme assets.

## M7 — Search, Settings and responsive nav rail (2026-07-05)

- Added the Search screen (sensors, hardware, settings).
- Added the Settings screen (theme, units, sampling speed, permissions, export folder, reset).
- Added a responsive navigation rail for wider screens.

## M6 — Recording + Export (2026-07-05)

- Room-backed recording of pinned sensor sessions.
- Export to CSV, JSON, XML, TXT and GPX.

## M5 — Camera info screen (2026-07-05)

- Per-camera sensor, resolution, focal length, FoV, zoom, OIS, flash and RAW details.

## M4 — GPS + Compass (2026-07-05)

- Live location screen with precise position, speed, bearing and accuracy.

## M3 — Sensors list, Sensor Detail and Live Monitor (2026-07-05)

- Full sensor list with live values.
- Per-sensor detail screen with live chart and statistics.
- Live Monitor screen for pinning sensors.

## M2 — Dashboard + Hardware with real device data (2026-07-05)

- Dashboard overview screen.
- Hardware screen: CPU, GPU, memory, storage, display, audio, connectivity.

## M1 — App shell: theme, bottom nav, 5 destinations (2026-07-05)

- Initial Compose app shell, Material 3 theme, bottom navigation with 5 destinations.

## M0 — Scaffold TrueSensor Android project (2026-07-05)

- Initial project scaffold (originally named TrueSensor, later renamed to TrueDroid and then to LabDroid).
