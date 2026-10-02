# Samsung TV Remote Android App — Initial Specification

## 1. Project Goal

Build a native Android application in Kotlin that acts as a full-featured remote control for a Samsung Q80R television over the local home network.

The app should replace the physical Samsung remote for normal day-to-day use and should be:

- Completely free
- Free of advertisements
- Free of subscriptions
- Free of analytics and tracking
- Local-network based
- Fast and responsive
- Simple to use
- Designed primarily for a phone held in portrait orientation

The first supported television is a Samsung Q80R.

The app should initially target Samsung TVs only. Do not attempt to make the first version universally compatible with all TV manufacturers.

However, the software architecture should make it reasonably easy to add support for other TV brands later.

---

# 2. Development Environment

Use:

- Android Studio
- Kotlin
- Jetpack Compose
- Material 3
- Kotlin Coroutines
- Gradle Kotlin DSL where appropriate

Use modern Android development practices.

Avoid unnecessary third-party libraries.

Prefer Android and Kotlin standard libraries unless an external dependency provides substantial value.

---

# 3. Core Design Principle

The Android application should act as a network remote control.

It should communicate directly with the Samsung TV over the local LAN/Wi-Fi network.

There should be:

- No cloud backend
- No external server
- No account system
- No remote database
- No advertising SDK
- No analytics SDK

All TV connection data should remain on the Android device.

---

# 4. Samsung TV Communication

The target Samsung Q80R supports Samsung's local-network remote-control protocol.

The application should investigate and use the Samsung WebSocket remote-control API.

Expected endpoints are likely to include:

```text
ws://<TV_IP>:8001/api/v2/channels/samsung.remote.control
```

and/or:

```text
wss://<TV_IP>:8002/api/v2/channels/samsung.remote.control
```

Do not blindly assume every protocol detail.

Before implementing the final communication layer, verify the protocol used by the Samsung Q80R generation and handle differences cleanly.

The TV may display a pairing/authorisation dialog when the Android app connects for the first time.

The application should support this pairing process.

If Samsung returns a persistent authentication token after pairing, store it locally and reuse it on subsequent connections.

---

# 5. Initial Remote Functions

The app should support the following core controls.

## Power

- Power
- Power Off
- Power On where supported by the TV/network configuration

Power-on may require Wake-on-LAN or another mechanism and should be treated separately from normal WebSocket commands if required.

---

## Volume

- Volume Up
- Volume Down
- Mute

Likely Samsung key commands include:

```text
KEY_VOLUP
KEY_VOLDOWN
KEY_MUTE
```

---

## Channels

- Channel Up
- Channel Down
- Previous Channel

Possible commands include:

```text
KEY_CHUP
KEY_CHDOWN
KEY_PRECH
```

---

# 6. TV Navigation

Navigation is a core requirement.

Provide:

- Up
- Down
- Left
- Right
- OK / Enter
- Back
- Exit
- Home
- Menu
- Settings
- Source

Possible Samsung commands include:

```text
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
```

Not all Samsung models necessarily support every key identically.

The app should make it easy to test and substitute equivalent key commands where necessary.

---

# 7. TV Menu Access

An important requirement is that the Android remote must allow the user to open and navigate the Samsung TV's own user interface.

The user should be able to use the Android app to access:

- TV Settings
- Picture settings
- Sound settings
- Network settings
- Source selection
- HDMI inputs
- Live TV
- Installed applications
- Samsung Home screen
- Guide
- Other menus normally accessible using the Samsung physical remote

The app does NOT need to recreate those Samsung menus itself.

Instead, it should open the relevant menu on the television and allow navigation using the remote's directional controls.

---

# 8. Main Remote Screen

The default screen should resemble a clean physical remote.

A proposed initial layout:

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

This layout is only an initial design.

The application should optimise button size and placement for comfortable one-handed operation.

Important controls should have large touch targets.

---

# 9. Secondary Controls

Provide access to less frequently used controls through either:

- A secondary screen
- Bottom sheet
- Expandable panel
- Tab
- More button

These may include:

## Numeric keypad

```text
1 2 3
4 5 6
7 8 9
  0
```

## Playback

- Play
- Pause
- Stop
- Rewind
- Fast Forward

## Other controls

- Guide
- Info
- Tools
- Exit
- Previous Channel

## Colour buttons

- Red
- Green
- Yellow
- Blue

These should only be included where Samsung supports corresponding remote commands.

---

# 10. Application Shortcuts

A future feature should allow direct launching of TV applications.

Examples:

- Netflix
- YouTube
- Amazon Prime Video
- ABC iview
- SBS On Demand
- 9Now
- 7plus
- 10
- Other installed Samsung apps

Do not make this part of the first milestone unless it is easy to implement after basic remote communication works.

The architecture should leave room for application launching later.

---

# 11. Connection Screen

Initially provide a simple way to connect to a TV.

The user should be able to enter:

```text
TV IP Address
```

For example:

```text
192.168.1.50
```

Provide:

- Connect button
- Disconnect button
- Current connection status
- Clear error messages

Possible states:

```text
Not configured
Connecting
Waiting for TV approval
Connected
Disconnected
Connection failed
```

---

# 12. Automatic TV Discovery

Automatic discovery should be implemented after direct IP connection works reliably.

Possible approaches may include:

- SSDP
- UPnP
- mDNS where applicable
- Samsung-specific discovery mechanisms
- Local subnet discovery

The app should eventually display detected TVs, for example:

```text
Samsung Q80R
192.168.1.50
```

The user should then be able to select the TV rather than manually entering its IP address.

Do not implement aggressive or unnecessary network scanning.

---

# 13. Connection Persistence

Remember the selected television.

Persist locally:

- TV name
- TV IP address
- Port/protocol
- Samsung pairing token where applicable
- Last successfully connected TV

On later app launches, attempt to reconnect automatically.

Do not make the user pair the phone every time.

---

# 14. Suggested Architecture

Use a clean separation between user interface and television communication.

Suggested structure:

```text
app/
    ui/
        RemoteScreen.kt
        ConnectionScreen.kt
        Components/

    tv/
        TvRemoteClient.kt
        SamsungRemoteClient.kt
        SamsungCommand.kt
        SamsungConnectionState.kt

    data/
        TvPreferences.kt
        TvDevice.kt

    viewmodel/
        RemoteViewModel.kt
```

This structure may evolve if there is a better reason to organise it differently.

---

# 15. Remote Client Abstraction

Create a generic interface such as:

```kotlin
interface TvRemoteClient {
    suspend fun connect()
    suspend fun disconnect()
    suspend fun sendCommand(command: RemoteCommand)
    val connectionState: StateFlow<ConnectionState>
}
```

Then implement:

```text
SamsungRemoteClient
```

This will make it easier to add other television manufacturers later.

Possible future clients might include:

```text
LgRemoteClient
AndroidTvRemoteClient
RokuRemoteClient
```

Do not implement those now.

---

# 16. Samsung Command Model

Do not scatter raw Samsung strings throughout the UI.

Create a central representation, for example:

```kotlin
enum class SamsungKey(val value: String) {
    VOLUME_UP("KEY_VOLUP"),
    VOLUME_DOWN("KEY_VOLDOWN"),
    MUTE("KEY_MUTE"),

    CHANNEL_UP("KEY_CHUP"),
    CHANNEL_DOWN("KEY_CHDOWN"),

    UP("KEY_UP"),
    DOWN("KEY_DOWN"),
    LEFT("KEY_LEFT"),
    RIGHT("KEY_RIGHT"),
    ENTER("KEY_ENTER"),

    BACK("KEY_RETURN"),
    HOME("KEY_HOME"),
    MENU("KEY_MENU"),
    SOURCE("KEY_SOURCE"),
    EXIT("KEY_EXIT")
}
```

Verify actual Samsung key support during implementation.

---

# 17. Button Behaviour

Remote-control buttons should feel responsive.

A normal press should immediately transmit the command.

Where useful, support press-and-hold behaviour.

Examples:

Holding:

```text
Volume Up
Volume Down
Channel Up
Channel Down
Directional buttons
```

should optionally send repeated commands at a sensible rate.

Avoid sending commands so rapidly that the TV becomes overloaded or input becomes uncontrollable.

---

# 18. Haptic Feedback

Provide subtle Android vibration/haptic feedback when a remote button is pressed.

This should make the phone feel more like a physical remote.

Provide an option later to disable haptic feedback.

---

# 19. UI Requirements

Use a clean, minimal interface.

Prefer:

- Large buttons
- High contrast
- Minimal visual clutter
- Clear icons
- Good spacing
- Portrait orientation
- Dark mode support

The main remote screen should require minimal scrolling.

Frequently used functions should always be immediately available.

Avoid trying to imitate the visual appearance of Samsung's official remote exactly.

Focus instead on usability.

---

# 20. Accessibility

Use appropriate Compose accessibility semantics.

Buttons should have meaningful content descriptions.

Touch targets should meet Android accessibility recommendations.

Do not rely purely on colour to communicate function.

---

# 21. Network Behaviour

The application should gracefully handle:

- TV turned off
- TV temporarily unavailable
- Wi-Fi disconnected
- Phone changing networks
- TV IP address changing
- WebSocket connection loss
- TV rejecting pairing
- Authentication token becoming invalid

Do not allow networking exceptions to crash the application.

Expose meaningful errors to the UI.

---

# 22. Security

The application communicates only with devices on the user's local network.

Do not transmit TV credentials, IP addresses or Samsung pairing tokens externally.

Store sensitive pairing information using an appropriate Android local-storage mechanism.

Do not log pairing tokens in production logs.

---

# 23. Logging

During development, provide useful logging for:

- Connection attempts
- Successful connection
- Disconnection
- Pairing events
- Samsung response messages
- Commands sent
- Protocol errors

Do not log sensitive authentication information.

Logging should be easy to reduce or disable for production builds.

---

# 24. Testing Strategy

Build the project incrementally.

Do not attempt to implement the entire application before verifying TV communication.

The first technical goal is simply:

```text
Android phone
      ↓
local Wi-Fi
      ↓
Samsung Q80R
      ↓
Volume increases
```

Once that works, progressively add functionality.

---

# 25. Development Milestones

## Milestone 1 — Project Skeleton

Create a new Android project containing:

- Kotlin
- Jetpack Compose
- Material 3
- Basic navigation
- Remote screen placeholder
- Connection screen

The app should compile and run.

---

## Milestone 2 — Samsung Connection Proof of Concept

Implement the minimum Samsung WebSocket client required to:

1. Enter the TV IP address
2. Connect to the Samsung TV
3. Complete TV authorisation/pairing
4. Send one command

Use:

```text
KEY_VOLUP
```

as the first test.

Success criterion:

Pressing a button on the Android phone increases the volume on the Samsung Q80R.

Do not proceed to extensive UI development until this works.

---

## Milestone 3 — Basic Remote

Add:

- Volume Up
- Volume Down
- Mute
- Up
- Down
- Left
- Right
- Enter
- Back
- Home

---

## Milestone 4 — TV Interface Controls

Add and test:

- Menu
- Settings
- Source
- Exit
- Guide

Determine which Samsung key codes work correctly on the Q80R.

---

## Milestone 5 — Full Remote Layout

Create the polished main remote UI.

Add:

- Channel controls
- Playback controls
- Numeric keypad
- Colour keys

---

## Milestone 6 — Persistent Pairing

Persist:

- TV address
- TV identity
- Pairing token
- Last connection information

Automatically reconnect where appropriate.

---

## Milestone 7 — Automatic Discovery

Find compatible Samsung TVs on the local network.

Allow the user to select one.

---

## Milestone 8 — Quality Improvements

Add:

- Haptic feedback
- Hold-to-repeat
- Better connection recovery
- Dark/light mode
- Settings screen
- User-configurable remote layout where practical

---

## Milestone 9 — Application Shortcuts

Investigate launching installed Samsung TV applications directly.

Add application buttons if supported reliably.

---

# 26. Future Features

Do not implement these initially, but keep the design extensible enough that they could be added later.

Possible features include:

- Multiple TVs
- Custom button layouts
- Favourite channels
- Favourite apps
- Widget/home-screen controls
- Android Quick Settings tile
- Voice commands
- Wear OS remote
- Wake-on-LAN
- Macro commands

Example macro:

```text
Watch Blu-ray
```

could eventually:

1. Power on TV
2. Select HDMI 2
3. Configure appropriate state

---

# 27. Universal Remote Expansion

The initial application is Samsung-specific.

However, avoid coupling the UI directly to Samsung protocol implementation.

The long-term architecture could become:

```text
Remote UI
     │
     ▼
TvRemoteClient
     │
 ┌───┼────────────┐
 ▼   ▼            ▼
Samsung   LG    Android TV
```

This should be an architectural consideration only.

Do not add unnecessary complexity simply to support hypothetical future devices.

---

# 28. Coding Style

Prefer:

- Readable Kotlin
- Small focused classes
- Coroutines rather than blocking network calls
- StateFlow for observable connection state
- Immutable UI state where practical
- Compose state hoisting
- Clear separation between UI and network implementation

Avoid:

- Huge classes
- Global mutable state
- Networking logic directly inside Composables
- Hard-coded IP addresses
- Raw command strings spread through the code
- Premature abstraction
- Unnecessary frameworks

---

# 29. Important Development Instruction

Develop this application incrementally.

After each major step:

1. Ensure the project builds.
2. Run it.
3. Test the new feature.
4. Resolve errors before continuing.

Do not generate the entire application in one large implementation pass.

The most important early proof is successfully controlling the actual Samsung Q80R.

---

# 30. First Task for Codex

Start with Milestones 1 and 2 only.

Create the Android application skeleton and implement the smallest possible Samsung TV communication proof of concept.

The first screen only needs:

```text
Samsung TV Remote

TV IP:
[________________]

[ Connect ]

Status: Disconnected

[ VOLUME + ]
```

The required behaviour is:

1. User enters the Samsung TV IP address.
2. User presses Connect.
3. The app attempts to establish the Samsung remote-control WebSocket connection.
4. Handle the Samsung TV's first-time pairing/approval process.
5. Display the connection state.
6. When connected, pressing VOLUME + sends the appropriate Samsung volume-up command.
7. Log useful WebSocket responses for debugging.

Stop after this proof of concept is working.

Do not yet build the complete remote-control interface.

Before writing Samsung protocol-specific code, verify the exact WebSocket handshake, authentication/token behaviour, message format and supported port/protocol appropriate for the Samsung Q80R generation.