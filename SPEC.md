# Samsung TV Remote — Product and Technical Specification

## 1. Product goal

Build a native Android application in Kotlin that acts as a practical replacement remote for a Samsung Q80R television over the local home network.

The app should be:

- Completely free to use
- Free of advertisements
- Free of subscriptions
- Free of analytics and tracking
- Local-network based
- Fast and responsive
- Simple to operate
- Designed primarily for a phone in portrait orientation
- The app is called Rogermote

The first supported television is the Samsung Q80R.

The first release is Samsung-specific. The internal design should avoid unnecessarily coupling the UI to Samsung protocol details so other TV families could be added later, but future-brand support must not complicate the initial implementation.

## 2. Development environment

Use:

- Kotlin
- Android Studio-compatible project
- Jetpack Compose
- Material 3
- Kotlin Coroutines
- Gradle Kotlin DSL where practical

Use modern Android development practices and avoid unnecessary dependencies.

## 3. Core architecture

The Android app communicates directly with the television on the local LAN/Wi-Fi network.

There is no:

- Cloud backend
- External control server
- User account system
- Remote database
- Advertising SDK
- Analytics SDK

All configuration and pairing information remains on the Android device.

Keep UI, state management, persistence, and TV protocol/network communication separated.

A reasonable eventual shape is:

```text
app/
  ui/
    RemoteScreen.kt
    ConnectionScreen.kt
    components/
  tv/
    TvRemoteClient.kt
    RemoteCommand.kt
    ConnectionState.kt
    samsung/
      SamsungRemoteClient.kt
      SamsungKey.kt
  data/
    TvDevice.kt
    TvPreferences.kt
  viewmodel/
    RemoteViewModel.kt
```

This is guidance, not a mandatory file tree. Prefer a simpler structure while the project is small.

## 4. Samsung communication

The target Samsung Q80R generation is expected to expose Samsung's local-network remote-control interface, commonly using a WebSocket-based protocol.

Likely endpoints on Samsung TVs include forms such as:

```text
ws://<TV_IP>:8001/api/v2/channels/samsung.remote.control
```

and/or:

```text
wss://<TV_IP>:8002/api/v2/channels/samsung.remote.control
```

These are candidates, not guaranteed requirements.

Before finalising Samsung protocol code, verify on the target TV or against reliable technical information:

- Supported port(s)
- `ws` versus `wss`
- TLS/certificate behaviour if applicable
- Required application/device-name encoding
- Initial authorization flow
- Pairing-token behaviour
- Reconnection behaviour
- Exact command message format
- Behaviour when authorization is rejected
- Behaviour when a stored token is invalid

The TV may display a first-time approval prompt. The app must support that interaction and clearly tell the user when TV approval is required.

If the TV provides a reusable pairing token, persist it securely and reuse it. Never log the token in production logs or documentation.

## 5. Remote command model

Do not place Samsung protocol strings directly in UI code.

Use an application-level command model, with Samsung-specific mapping in the Samsung client.

Likely Samsung key names to investigate include:

```text
KEY_VOLUP
KEY_VOLDOWN
KEY_MUTE
KEY_CHUP
KEY_CHDOWN
KEY_PRECH
KEY_UP
KEY_DOWN
KEY_LEFT
KEY_RIGHT
KEY_ENTER
KEY_RETURN
KEY_EXIT
KEY_HOME
KEY_MENU
KEY_SOURCE
KEY_GUIDE
```

These are candidates to test, not a declaration that every key is supported identically on the Q80R.

## 6. Core controls

The completed remote should support, where the Q80R supports the corresponding operation:

### Power

- Power off
- Power on

Power-on must be treated separately if normal WebSocket control is unavailable while the TV sleeps. Investigate Wake-on-LAN or Samsung-specific alternatives only after basic connected control works.

### Volume

- Volume up
- Volume down
- Mute

### Channels

- Channel up
- Channel down
- Previous channel

### Navigation

- Up
- Down
- Left
- Right
- OK / Enter
- Back
- Exit
- Home

### TV interface access

Provide convenient access to the TV's own interface, including where supported:

- Home
- Menu
- Settings
- Source
- Guide

The Android app does not need to recreate Samsung's settings, source picker, or app browser. It should open the relevant TV UI and allow the user to navigate it with the D-pad and OK/Back controls.

This should enable access to:

- Picture settings
- Sound settings
- Network settings
- HDMI/input selection
- Live TV
- Installed applications
- Samsung Home
- Guide
- Other normal TV menus

If there is no dedicated Samsung key that opens a desired screen reliably, use the most dependable navigation path rather than inventing unsupported protocol behaviour.

## 7. Main remote UI

The default screen should feel like a clean physical remote.

Initial conceptual layout:

```text
┌────────────────────────────┐
│ Power    Source   Settings │
│                            │
│            Home            │
│                            │
│             ▲              │
│                            │
│        ◀    OK    ▶        │
│                            │
│             ▼              │
│                            │
│            Back            │
│                            │
│  VOL +              CH +   │
│                            │
│  VOL -              CH -   │
│                            │
│  Mute               Guide  │
└────────────────────────────┘
```

The final layout may differ after real-device testing.

Requirements:

- Large touch targets
- Comfortable one-handed operation
- Minimal visual clutter
- High contrast
- Clear icons/text
- Portrait-first design
- Dark mode support
- Minimal scrolling for common controls

Do not copy Samsung's visual design unnecessarily. Optimize for usability.

## 8. Secondary controls

Less frequently used controls may live on a secondary screen, bottom sheet, expandable panel, tab, or similar UI.

Potential controls:

### Numeric keypad

```text
1 2 3
4 5 6
7 8 9
  0
```

### Playback

- Play
- Pause
- Stop
- Rewind
- Fast forward

### Other

- Info
- Tools
- Exit
- Previous channel
- Guide
- Red/Green/Yellow/Blue function keys

Only expose controls that can be made reliable on the target TV.

## 9. TV application shortcuts

A later feature may directly launch installed TV applications, such as:

- Netflix
- YouTube
- Prime Video
- ABC iview
- SBS On Demand
- 9Now
- 7plus
- 10
- Other installed Samsung apps

Do not make app launching part of the initial proof of concept.

Before implementation, verify the Q80R's supported app-discovery/launch mechanism and app identifiers. Do not hard-code unverified IDs copied from unrelated models.

## 10. Connection screen

The initial implementation must support manual TV configuration.

Provide:

- TV IP-address input
- Connect
- Disconnect
- Connection status
- Useful error messages

Example:

```text
Samsung TV Remote

TV IP:
[ 192.168.x.x          ]

[ Connect ]

Status: Disconnected

[ VOLUME + ]
```

Possible UI states include:

- Not configured
- Connecting
- Waiting for TV approval
- Connected
- Disconnected
- Authorization rejected
- Connection failed

The UI should distinguish a pairing/approval wait from a generic network failure when the protocol makes that possible.

## 11. Automatic discovery

Automatic TV discovery is a later milestone and must not block manual-IP control.

Potential mechanisms to investigate include:

- SSDP
- UPnP
- mDNS if relevant
- Samsung-specific discovery mechanisms

Avoid aggressive subnet scanning.

The eventual UI should allow a discovered television to be selected and should show useful identifying information such as its friendly name and IP address.

## 12. Persistence

Eventually remember:

- Selected TV/friendly name
- TV IP address
- Relevant port/protocol
- Pairing token where applicable
- Last successfully connected TV

On later launches, reconnect automatically where appropriate.

The app should tolerate the TV's DHCP address changing. Automatic discovery or a user-editable address can address this later.

## 13. Suggested abstraction

Keep protocol implementation behind a small interface. For example:

```kotlin
interface TvRemoteClient {
    val connectionState: StateFlow<ConnectionState>

    suspend fun connect()
    suspend fun disconnect()
    suspend fun sendCommand(command: RemoteCommand)
}
```

A Samsung implementation can then map generic commands to Samsung protocol messages.

Do not create LG/Roku/Android TV implementations now.

## 14. Button behaviour

A normal tap should send the command promptly.

Later, press-and-hold may repeat suitable commands such as:

- Volume up/down
- Channel up/down
- Directional navigation

Repeat at a controlled rate. Avoid command floods or runaway input.

## 15. Haptic feedback

Provide subtle Android haptic feedback for remote button presses in a later polish milestone.

Eventually provide a setting to disable haptics.

## 16. Accessibility

Use appropriate Compose semantics and meaningful content descriptions.

Use adequately sized touch targets.

Do not rely solely on colour to communicate meaning.

## 17. Network and lifecycle behaviour

The app must eventually handle without crashing:

- TV powered off
- TV temporarily unavailable
- Wi-Fi disconnected
- Phone changing networks
- WebSocket loss
- TV rejecting pairing
- Stored token becoming invalid
- App moving between foreground/background
- TV IP address changing

Networking must not block the Android main thread.

## 18. Android network considerations

Declare only permissions genuinely required for the implemented features.

For local-network/WebSocket communication, investigate current Android platform requirements rather than copying obsolete manifest settings.

If cleartext `ws://` is required by the Q80R, configure network security narrowly and deliberately rather than enabling broad insecure traffic without explanation.

If `wss://` uses a certificate that Android does not normally trust, do not globally disable TLS verification. Handle the target-TV case narrowly and document the security trade-off.

## 19. Security and privacy

- Keep control traffic local wherever possible.
- Do not send configuration or usage information to external services.
- Do not log pairing tokens.
- Do not commit secrets.
- Store authentication material using an appropriate Android storage mechanism.
- Validate user-entered addresses sufficiently to avoid obvious malformed configuration.
- Do not introduce an embedded web server or remotely accessible control interface unless a future requirement explicitly calls for it.

## 20. Logging

Development logging should help diagnose:

- Connection attempts
- Connection success/failure
- Disconnection
- Pairing state
- TV protocol responses
- Commands sent
- Protocol errors

Redact authentication material.

Production logging should be minimal.

## 21. Testing strategy

Develop incrementally.

The first important end-to-end proof is:

```text
Android phone
     ↓
home Wi-Fi
     ↓
local network
     ↓
Samsung Q80R
     ↓
TV volume increases
```

Do not build the complete UI before this is proven.

Use unit tests for protocol serialization/parsing and state logic where useful. Physical-TV behaviour must be confirmed on the actual Q80R.

## 22. Future features

Do not implement these initially:

- Multiple TVs
- Custom button layouts
- Favourite channels
- Favourite apps
- Home-screen widgets
- Android Quick Settings tile
- Voice commands
- Wear OS remote
- Wake-on-LAN
- Macros
- Other TV brands

Possible future macro:

```text
Watch Blu-ray
```

could eventually power on the TV and select a particular input, but only after the underlying operations are reliable.

## 23. Universal-remote expansion

The long-term conceptual architecture may become:

```text
Remote UI
    │
    ▼
TvRemoteClient
    │
 ┌──┼───────────┐
 ▼  ▼           ▼
Samsung   LG   Android TV
```

This is a future architectural direction, not a current deliverable.

## 24. Product success criteria

The app is successful when it can reliably replace the commonly used functions of the physical Q80R remote without ads, subscriptions, tracking, or cloud dependency.

Reliability and simplicity are more important than maximizing the number of buttons.
