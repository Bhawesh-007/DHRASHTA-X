# GUARDIAN — On-Device Mobile Threat Defense

> A passive, offline, on-device network threat detector for Android phones.
> Built for the iQOO City Battles hackathon, Track 6 (Digital Security).

---

## 1. The Problem

Every Android phone in India runs dozens of applications. Most users have zero
visibility into what those apps do on the network.

The threat is real and documented:

- Commercial spyware (Pegasus, Predator) beacons to a fixed destination at
  regular intervals.
- Stalkerware (spouse-tracking apps) exhibits the same pattern.
- Malicious apps phone home with asymmetric upload traffic.
- Ad SDKs send DNS queries with high entropy.

These behaviours are all visible on the wire, but no consumer Android app
watches for them without root, without cloud, without draining the battery.

Enterprise MDM solutions exist, but they are for corporate-managed devices.
Personal phones have no equivalent protection.

## 2. The Solution

**GUARDIAN** is a passive network monitor built into a VpnService-tunnelled
Android app. It:

1. Intercepts all outbound device traffic using `VpnService` (no root required)
2. Extracts 74 flow-level behavioural features per 5-second window
3. Runs a small quantized XGBoost model on the Snapdragon NPU
4. Flags anomalies: periodic beaconing, exfiltration asymmetry, high-entropy
   DNS, unknown TLS fingerprints
5. Shows the user a plain-language alert with evidence

Nothing leaves the device. No cloud. No telemetry. No root.

## 3. Why This Wins at iQOO City Battles

### The scoring rubric

| Source            | Weight | What it measures                       |
|-------------------|--------|----------------------------------------|
| Jury              | 75%    | Pitch, depth, novelty, demo quality    |
| Device data       | 15%    | Automatic measurement of phone usage   |
| HackTracker       | 10%    | Efficiency metrics over 36 hours       |
| Office Kit usage  | 10%    | Screen mirror + shared clipboard       |

### How GUARDIAN hits each source

**Device data (15%):**
- Uses the NPU via TFLite NNAPI delegate
- Uses the microphone for voice alerts
- Uses camera for trusted-app QR whitelisting
- Sensors: ConnectivityManager to distinguish Wi-Fi vs cellular

**Jury (75%):**
- Passive monitoring is a difficult, technical problem
- On-device inference is timely and impressive
- Demo is dramatic — install malware, watch it get caught
- Real-world impact is defensible

**HackTracker (10%):**
- Green Light: heavy commit density on Android scaffolding
- Red Light: sustained foreground app usage on phone-only work
- Both phases have measurable phone activity

**Office Kit (10%):**
- Screen mirror during demo shows phone UI on laptop screen
- Shared clipboard used during code transfer

### Track fit

Track 6 (Digital Security) is under-subscribed compared to FinTech and EdTech.
GUARDIAN competes in a smaller pool of strong entries.

## 4. Architecture
