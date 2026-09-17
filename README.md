# DHRASHTA-X

DHRASHTA-X is an offline-first Android mobile-threat-defense prototype. The
current build uses explicit heuristic rules and local evidence instead of an
ML model.

## Current capabilities

- Kotlin and Jetpack Compose interface
- VPN consent and foreground observer-service lifecycle
- IPv4/IPv6, TCP, UDP and DNS metadata parsing
- UID/package attribution and bounded flow aggregation
- Rule-based alerts for beaconing, exfiltration, DNS tunnelling, scanning and
  risky sideloaded-app behavior
- Local Room persistence, alert acknowledgement and history deletion
- Notifications, optional voice alerts and trusted-package management
- CameraX/ML Kit QR whitelist import
- Local egress counter and a safe end-to-end demo mode

The app clearly labels every current verdict as rule-based. ONNX inference is
not included until the frozen model, schema, scaler and test vectors are
available and verified.

## Important networking boundary

The current `VpnService` runs in safe observer mode and deliberately installs
no default route. Full-device packet capture is disabled until a protected
user-space TCP/UDP forwarder is integrated. Enabling a default route without
that forwarder would disconnect the device.

See [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) for the exact verified
scope and deferred work.

## Build

The project uses Java 17, Gradle 8.2.1 and Android API 34.

```sh
. scripts/android-env.sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

If you are using Android Studio, open the repository and select a Java 17 JDK.
The project-local toolchain described in [ANDROID_SETUP.md](ANDROID_SETUP.md)
is ignored by Git and is not downloaded with the repository.

## Verified checks

- 12 unit tests passing
- Android lint passing
- Debug APK assembly passing
- Package: `com.dhrashtax`
- Minimum SDK: 29
- Target SDK: 34

Physical-device validation is still required because no Android device was
connected during the latest verification run.

