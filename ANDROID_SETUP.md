# DHRASHTA-X Android setup

This repository uses a project-local Android toolchain. No system-wide Java,
Gradle, or Android SDK installation is required.

## Document-derived baseline

- Package: `com.dhrashtax`
- Kotlin and Jetpack Compose
- `minSdk = 29`, `targetSdk = 34`, `compileSdk = 34`
- Java 17 bytecode
- Room, CameraX, ML Kit barcode scanning and Compose Navigation
- Model inference is deliberately excluded from the current heuristic build

## Use the environment

```sh
. scripts/android-env.sh
./gradlew --version
./gradlew :app:assembleDebug
adb devices
```

The debug APK will be written under `app/build/outputs/apk/debug/`.

Run the verified checks with:

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

## Assets still required by the handoff

The source documents refer to files that are not included in this directory:

- `models/mlp_network_v2_qdq.onnx`
- `models/mlp_network_v2_float.onnx`
- `schema/feature_schema.json`
- `schema/scaler.json`
- `test-data/test_vector.json`

Do not invent replacements. The application currently identifies itself as a
rule-based heuristic edition and does not include ONNX Runtime. Once these
files are supplied, model integration should be added behind the existing
detector boundary and all 13 vectors must pass before an ML verdict is shown.

## Optional emulator

No emulator system image is installed. The project can build and connect to a
physical iQOO device through ADB. Add an API 34 emulator later only if needed.

## Observer-mode boundary

The foreground `VpnService` intentionally installs addresses but no default
route. This lets the permission, lifecycle and foreground-service paths be
tested without disrupting internet access. Full-device packet capture remains
disabled until a protected user-space TCP/UDP forwarder is integrated. The UI
states this limitation explicitly.

