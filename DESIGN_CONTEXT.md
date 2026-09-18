# DHRASHTA-X Design Context

This document is the source of truth for future design and UI implementation work on DHRASHTA-X.

## Product

- Name: DHRASHTA-X
- Tagline: On-Device Android Threat Intelligence
- Purpose: A privacy-first Android security app that monitors network behavior and device context on-device to detect Banking C2, spyware, stalkerware, and adware.
- Product promise: Clear, explainable threat intelligence that continues to protect the device without a cloud connection.

## Design Direction

The visual direction is inspired by the broad design language of the Dribbble shot [Cybersecurity Mobile App UI - Threat Alerts & Security Dashboard](https://dribbble.com/shots/27151872-Cybersecurity-Mobile-App-UI-Threat-Alerts-Security-Dashboard): clean light surfaces, deep security blue, soft blue accents, rounded white cards, subtle shadows, generous whitespace, and simple information hierarchy.

The product must remain an original DHRASHTA-X design. Do not copy the reference composition or artwork. Do not use dark mode, neon green, cyberpunk effects, glowing elements, or dense hacker-themed visuals.

## Design Tokens

| Token | Value | Usage |
| --- | --- | --- |
| App background | `#F2F2F3` | Screen canvas |
| Card background | `#FFFFFF` | Cards, sheets, navigation |
| Primary blue | `#2244D5` | Main actions, progress, active states |
| Soft blue | `#9DDCFB` | Quiet fills and charts |
| Light blue | `#73C6FC` | Secondary chart segments |
| Cyan | `#58B3FB` | Supporting data accents |
| Primary text | `#262728` | Titles, metrics, high-emphasis labels |
| Secondary text | `#6B6D72` | Supporting copy and metadata |
| Border | `#E4E5E8` | Thin card and divider outlines |
| Critical | `#E5484D` | Small severity badges and icons only |
| High | `#F59E0B` | Small severity badges and icons only |
| Medium | `#F5C542` | Small severity badges and icons only |
| Secure/resolved | `#22A06B` | Small success badges and icons only |

## Typography and Layout

- Typeface: Inter, with Android system sans-serif as fallback.
- Use large, bold type for security scores and threat names.
- Maintain generous whitespace and a clear vertical rhythm.
- Primary horizontal screen padding: 20 dp.
- Card radius: 20 dp.
- Pill and button radius: fully rounded or 16-24 dp depending on height.
- Card treatment: white fill, 1 dp `#E4E5E8` border, restrained soft shadow.
- Iconography: simple rounded outline icons with a consistent stroke weight.
- Touch targets: minimum 48 x 48 dp.

## Navigation Model

Use a persistent bottom navigation on top-level screens with four destinations:

1. Home
2. Alerts
3. Protection
4. Offline

Threat Detail is a pushed screen reached from Home or Alerts and uses a back action instead of bottom navigation.

## Required Screens

### 1. Home / Security Dashboard

- Top bar: `DHRASHTA-X` and notification icon.
- Security hero: score `94`, circular progress ring, status `Protected`, and `Your device is being monitored continuously.`
- Primary CTA: `Run Security Check`.
- 2 x 2 summary grid:
  - `Threats Detected` / `03`
  - `Threats Resolved` / `12`
  - `Apps Monitored` / `47`
  - `Network Flows` / `1,284`
- Active Threats cards contain a severity badge, threat name, concise explanation, confidence, and `Investigate` action.

### 2. Threat Alerts

- Header: `Threat Alerts`.
- Filter pills: `All`, `Critical`, `High`, `Medium`, `Resolved`.
- Every row includes threat name, severity badge, confidence, timestamp, short explanation, affected app, and chevron.
- Cards expand and collapse smoothly to reveal additional context and actions.

### 3. Threat Detail

- Header: `Threat Detected` with back navigation.
- Identity block: `BANKING_C2`, Critical badge, and `91% Confidence` circular visualization.
- Explanation card:
  - Title: `Why was this detected?`
  - Body: `Regular outbound communication was detected. The application also has accessibility access and was recently sideloaded.`
- Detection Evidence bars:
  - `Beacon Regularity` / `92%`
  - `Upload Size` / `78%`
  - `Flow Repeat Ratio` / `71%`
  - `TLS Sessions` / `64%`
- Device Context items:
  - `Accessibility service enabled`
  - `App recently sideloaded`
  - `Unknown sources enabled`
  - `Notification listener detected`
- Actions: primary `Resolve`, secondary `Ignore`.

### 4. Protection

- Header: `Protection`.
- Large control cards with icon, title, explanation, and switch:
  - Real-Time Monitoring: ON
  - Network Monitoring: ON
  - Threat Detection: ON
  - Permission Monitoring: ON
  - Offline Protection: ON
  - Voice Alerts: OFF

### 5. Offline Protection

- Header: `Offline Protection`.
- Hero status: green checkmark and `PROTECTION ACTIVE`.
- Subtitle: `Your device is protected without an internet connection.`
- Status rows:
  - Cloud Connection: OFF
  - Local ML Model: ACTIVE
  - Network Monitoring: ACTIVE
  - Threat Detection: ACTIVE
- Main visual: stylized airplane icon, `AIRPLANE MODE`, and `Detection continues offline.`

## Interaction Guidance

- Dashboard score counts from 0 to 94 with a synchronized circular progress animation.
- Threat cards expand and collapse with a 220-280 ms ease-out transition.
- Resolving a threat uses a short checkmark and card-state success animation; avoid confetti.
- `Run Security Check` shows a restrained blue scanning sweep and progress feedback.
- Switches animate smoothly and always expose their state through text or accessibility semantics, not color alone.
- Respect Android reduced-motion and TalkBack settings.

## Content and Safety Rules

- Explain why a threat was detected before asking the user to act.
- Always show severity, confidence, affected app, evidence, and device context where available.
- Semantic colors are accents only; never flood full cards with red, orange, yellow, or green.
- Never claim cloud analysis when offline protection is active.
- The mockups represent the target product design. Labels such as `Local ML Model: ACTIVE` are design requirements and must not be treated as proof that the current prototype already ships a trained model.

## Deliverables

High-fidelity mockups are stored under `design/mockups/`. Any future UI implementation should be checked against this document for palette, copy, hierarchy, component styling, and interaction behavior.
