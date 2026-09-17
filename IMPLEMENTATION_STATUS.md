# DHRASHTA-X heuristic implementation status

## Implemented

- Kotlin/Compose application with onboarding, dashboard, alert detail,
  settings, privacy and QR whitelist screens
- Foreground `VpnService` lifecycle and VPN consent flow
- Safe observer TUN configuration with no default route
- IPv4/IPv6, TCP, UDP and DNS metadata parser
- UID/package attribution adapter for API 29+
- Bounded five-second flow aggregation
- Rule-based detection for periodic beaconing, exfiltration asymmetry,
  high-entropy DNS, scanning and risky sideloaded-app context
- Best-effort accessibility, notification-listener, device-admin, overlay and
  install-source context collection
- One-minute alert deduplication
- Room alert persistence and exported version-1 schema
- Alert acknowledgement and clear-history controls
- Local notifications and optional text-to-speech alerts
- Manual and CameraX/ML Kit QR trusted-package import
- App-egress counter and local-data privacy explanation
- Safe demo traffic snapshots that exercise the full detector and UI path
- Unit tests covering packet parsing, DNS parsing, aggregation, QR validation
  and detector rules

## Explicitly deferred

- ONNX model/scaler/schema loading and ML inference
- Default-route packet interception
- TCP/UDP user-space forwarding
- Hardware acceleration through NNAPI/QNN
- Exact frozen 82-feature parity testing

Default-route capture and forwarding are one inseparable gate: routing all
traffic into the TUN before a protected forwarder is working would disconnect
the device. The current app therefore reports `captureOperational = false`
instead of presenting observer mode as full protection.

## Verification commands

```sh
. scripts/android-env.sh
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :app:assembleDebug
adb devices
```

