# Samsung TV Remote — Implementation Plan

Work sequentially. Do not treat later milestones as permission to implement them early.

## Milestone 1 — Project skeleton

### Goal

Create the smallest clean Android application foundation.

### Tasks

- [ ] Create/configure Android project.
- [ ] Use Kotlin.
- [ ] Enable Jetpack Compose.
- [ ] Use Material 3.
- [ ] Establish a simple package structure.
- [ ] Add a basic connection/remote proof-of-concept screen.
- [ ] Add state holder/ViewModel only as needed.
- [ ] Ensure project builds successfully.
- [ ] Launch the app on an emulator or physical Android device where available.
- [ ] Update `STATUS.md`.

### Initial UI

The first screen may contain:

```text
Samsung TV Remote

TV IP:
[________________]

[ Connect ]

Status: Disconnected

[ VOLUME + ]
```

`VOLUME +` does not need to control the TV during Milestone 1.

### Completion criteria

- Project compiles.
- App launches without crashing.
- Initial screen renders.
- TV IP can be entered in the UI.
- No Samsung network implementation is required yet.

### Stop point

After Milestone 1 is built and verified, stop and report the result. Do not begin Milestone 2 until authorized.

---

## Milestone 2 — Samsung connection proof of concept

### Goal

Prove that the Android phone can pair/connect to the actual Samsung Q80R and send one real command.

The initial TV authorization should be persistent. After the user approves the app on the Samsung TV for the first time, subsequent normal connections should reuse the stored authorization and should not repeatedly cause the TV to display an "Allow" prompt.

### Research/verification first

Before committing to protocol code:

- [ ] Verify the likely Samsung Q80R local remote-control endpoint/port.
- [ ] Verify WebSocket request/message format.
- [ ] Verify how the app/device name must be represented.
- [ ] Verify first-time authorization/pairing behaviour.
- [ ] Determine whether a token is returned and how it is reused.
- [ ] Determine whether the pairing token remains valid after the Android app is closed and reopened.
- [ ] Determine whether the pairing token remains valid after the TV is turned off and back on.
- [ ] Determine how to detect and handle an invalid or expired pairing token.
- [ ] Determine how TLS/certificates must be handled if secure WebSocket is used.
- [ ] Record confirmed findings in `STATUS.md`.

Do not assume candidate protocol details in `SPEC.md` are guaranteed.

### Implementation

- [ ] Use the TV IP entered by the user.
- [ ] Implement the minimum Samsung connection client.
- [ ] Connect/disconnect without blocking the main thread.
- [ ] Expose meaningful connection state.
- [ ] Show when the TV is waiting for user approval if detectable.
- [ ] Handle authorization rejection cleanly.
- [ ] Implement the smallest required pairing-token persistence if the target TV requires it.
- [ ] Persist the pairing/authentication token securely on the Android device.
- [ ] Reuse the stored pairing token on subsequent connections.
- [ ] Reuse a stable app/device identity if required by the Samsung protocol.
- [ ] Do not initiate a new pairing request when a valid stored authorization exists.
- [ ] Do not discard a valid pairing token because of an ordinary temporary connection failure.
- [ ] If the stored authorization becomes invalid, handle this cleanly and initiate re-pairing only when necessary.
- [ ] Never log or write the actual pairing token to `STATUS.md`.
- [ ] Implement one command only: volume up.
- [ ] Add development logging with secrets redacted.
- [ ] Add focused tests for protocol serialization/parsing where practical.
- [ ] Build successfully.
- [ ] Update `STATUS.md`.

### Physical test procedure

#### Initial pairing and command test

1. Phone and TV are on the same home LAN.
2. Enter the Q80R's local IP address.
3. Tap Connect.
4. Approve the phone/app on the TV if prompted.
5. Wait for Connected state.
6. Tap `VOLUME +`.
7. Confirm that the TV volume actually increases.

#### Pairing persistence test

8. Completely close the Android app.
9. Reopen the Android app.
10. Connect to the same Samsung Q80R.
11. Confirm that the TV does **not** display another "Allow" prompt.
12. Tap `VOLUME +`.
13. Confirm that the TV volume still increases.

#### TV restart persistence test

14. Turn the Samsung Q80R off normally.
15. Turn the TV back on.
16. Wait for its network connection to become available.
17. Reopen/connect the Android remote.
18. Confirm that the TV does **not** display another "Allow" prompt.
19. Tap `VOLUME +`.
20. Confirm that the TV volume still increases.

#### Repeated connection test

21. Disconnect and reconnect the app several times.
22. Confirm that normal reconnections do **not** cause repeated "Allow" prompts on the Samsung TV.
23. Confirm that `VOLUME +` continues to work.

### Completion criteria

Milestone 2 is complete only when the user confirms that:

- [ ] Pressing `VOLUME +` on the Android phone physically increases the volume on the Samsung Q80R.
- [ ] The initial Samsung authorization/pairing information is persisted.
- [ ] Closing and reopening the Android app does not normally cause another "Allow" prompt.
- [ ] Turning the TV off and back on does not normally cause another "Allow" prompt.
- [ ] Repeated normal connections do not cause repeated "Allow" prompts.
- [ ] Temporary connection failures do not unnecessarily discard valid authorization.
- [ ] If authorization genuinely becomes invalid, the app handles re-pairing cleanly.

A successful build or WebSocket connection alone is not sufficient.

Repeated Samsung "Allow" prompts during normal use are considered a failure of Milestone 2 unless testing establishes that the TV itself has invalidated the authorization for a reason outside the application's control.

### Stop point

After successful physical verification, update `STATUS.md` and stop. Do not automatically continue to Milestone 3.

---
## Milestone 3 — Basic remote

### Goal

Turn the proof of concept into the essential everyday remote.

### Controls

- [ ] Volume up
- [ ] Volume down
- [ ] Mute
- [ ] Up
- [ ] Down
- [ ] Left
- [ ] Right
- [ ] OK / Enter
- [ ] Back
- [ ] Home

### Engineering

- [ ] Introduce/complete an application-level `RemoteCommand` model.
- [ ] Centralize Samsung key mappings.
- [ ] Keep raw Samsung command strings out of Composables.
- [ ] Handle command-send failures without crashing.
- [ ] Add tests for mappings/serialization where useful.
- [ ] Build successfully.

### Physical verification

Test every control on the actual Q80R.

### Completion criteria

All listed controls work reliably on the physical TV.

---

## Milestone 4 — TV interface controls

### Goal

Provide convenient access to Samsung's own menus so the phone can replace the physical remote for settings, inputs, and TV navigation.

### Investigate and add

- [ ] Source
- [ ] Settings or reliable path to Settings
- [ ] Menu where applicable
- [ ] Exit
- [ ] Guide

### Verification

For each feature:

- [ ] Determine the reliable Q80R command/navigation path.
- [ ] Test it on the physical TV.
- [ ] Record model-specific findings in `STATUS.md`.

### Completion criteria

Using only the Android app, the user can reach and navigate the TV UI needed for:

- Settings
- Source/input selection
- App selection/Home
- Guide where supported

---

## Milestone 5 — Full remote layout

### Goal

Create a polished, comfortable primary remote UI.

### UI

- [ ] Refine portrait layout.
- [ ] Large accessible touch targets.
- [ ] Home/navigation cluster.
- [ ] Volume/channel controls.
- [ ] Source/settings access.
- [ ] Mute.
- [ ] Guide.
- [ ] Dark mode support.
- [ ] Minimal scrolling for common actions.

### Secondary controls

Where supported and useful:

- [ ] Numeric keypad
- [ ] Play
- [ ] Pause
- [ ] Stop
- [ ] Rewind
- [ ] Fast forward
- [ ] Info
- [ ] Previous channel
- [ ] Red/Green/Yellow/Blue keys

### Completion criteria

The remote is comfortable for routine use and all exposed buttons have been tested against the Q80R.

---

## Milestone 6 — Persistent configuration and pairing

### Goal

Make normal launches require minimal setup.

### Tasks

- [ ] Persist selected TV information.
- [ ] Persist IP address.
- [ ] Persist protocol/port if necessary.
- [ ] Persist pairing token securely if used.
- [ ] Never log the token.
- [ ] Reconnect appropriately on later launches.
- [ ] Allow configuration to be changed/reset.
- [ ] Recover cleanly from invalid/expired authorization.

### Completion criteria

After initial setup, the app can be closed and reopened and can reconnect/control the TV without unnecessary re-pairing.

---

## Milestone 7 — Automatic TV discovery

### Goal

Remove the need to manually type an IP address for normal setup.

### Tasks

- [ ] Research the least intrusive reliable discovery method for the Q80R.
- [ ] Prefer standard discovery mechanisms over aggressive subnet scanning.
- [ ] Discover compatible Samsung TV(s).
- [ ] Show friendly name/IP where available.
- [ ] Allow the user to select a TV.
- [ ] Retain manual IP entry as a fallback.

### Completion criteria

The Q80R can normally be selected from a discovered-device list on the home network.

---

## Milestone 8 — Quality and resilience

### Goal

Make the app pleasant and robust in everyday use.

### Tasks

- [ ] Haptic feedback.
- [ ] Setting to disable haptics.
- [ ] Hold-to-repeat for appropriate controls.
- [ ] Controlled repeat rate.
- [ ] Better reconnection behaviour.
- [ ] Handle Wi-Fi/network changes.
- [ ] Handle TV unavailable/offline.
- [ ] Handle app foreground/background transitions.
- [ ] Improve user-facing errors.
- [ ] Accessibility review.
- [ ] UI polish.
- [ ] Settings screen if useful.

### Completion criteria

Common network interruptions and TV-offline cases do not crash the app, and normal recovery is understandable to the user.

---
## Milestone 8.5 — Remote UI Revamp

### Goal

Redesign the application UI so that configuration and everyday remote-control functions are separated into two pages, while completely revamping the Remote Control page into a compact, polished interface based closely on the supplied reference image.

The new remote should feel like a purpose-built physical remote rather than a collection of standard Android buttons.

All primary remote controls must fit on a single phone screen without scrolling.

---

### Primary Visual Reference

The primary visual reference for this milestone is:

```text
C:\junk\Screenshot_20261002_121607.png
```

**This image is a major design reference and showpiece for this milestone.**

Codex must inspect this image before implementing the new Remote Control UI.

The objective is not necessarily a pixel-for-pixel copy, but the new Remote Control screen should closely follow its overall visual language, proportions, control arrangement and physical-remote feel.

In particular, use the reference for:

- Overall dark remote-control appearance
- Compact layout
- Button shapes
- Button sizing
- Button spacing
- Visual hierarchy
- Rounded controls
- Navigation-pad design
- Large circular directional control area
- Small secondary buttons
- Volume/channel vertical controls
- Bottom More Controls treatment
- General proportions of the remote

Do not simply restyle the existing large white Material buttons.

The reference image should be treated as the target visual direction for the new remote.

---

## Two-Page Application Structure

Refactor the application into two primary pages:

1. **TV Setup / Settings**
2. **Remote Control**

The everyday remote controls should no longer share a page with TV discovery, saved-TV management or haptic configuration.

---

## Page 1 — TV Setup / Settings

### Purpose

This page contains functions related to configuring the TV and application rather than everyday remote operation.

### Controls

Move the following existing functions to this page:

- [ ] Scan for TVs
- [ ] Forget Saved TV
- [ ] Haptic Feedback toggle

Also retain any TV-selection or connection information required to make the existing discovery/pairing workflow understandable.

The existing working behaviour for these features must not be lost during the UI refactor.

### TV Discovery

The user must still be able to:

1. Scan for TVs.
2. Select/pair with the Samsung TV.
3. Save the paired TV/IP using the application's existing persistence mechanism.
4. Proceed to the Remote Control page after successful connection.

### Forget Saved TV

`Forget Saved TV` must:

- Clear the saved TV configuration using the existing application behaviour.
- Clear associated pairing information when appropriate.
- Return the application to a state where another TV can be discovered or configured.
- Not accidentally trigger repeated Samsung authorization prompts during normal use.

### Haptic Feedback

The existing Haptic Feedback setting must remain persistent.

Moving the toggle to the TV Setup / Settings page must not change its behaviour.

---

## Page 2 — Remote Control

### Purpose

This is the primary everyday screen.

Once a TV has been configured, the user should normally open the application and arrive directly on this page.

The screen must contain all commonly used remote controls without requiring vertical scrolling.

---

### Startup Behaviour

When the application starts:

1. Check for the previously saved/paired TV.
2. If a saved TV/IP exists, restore it automatically.
3. Attempt to reconnect using the existing saved pairing/authorization information.
4. Navigate directly to the **Remote Control** page.
5. Do not require the user to scan for or select the same TV again during normal use.
6. Do not initiate unnecessary Samsung re-pairing.
7. Reuse the existing persisted Samsung authorization/token behaviour.

The normal startup experience should therefore be:

```text
Launch app
    ↓
Saved TV found
    ↓
Restore saved IP and pairing information
    ↓
Reconnect
    ↓
Remote Control page
```

If no saved TV exists:

```text
Launch app
    ↓
No saved TV
    ↓
TV Setup / Settings page
    ↓
Scan/select/pair TV
    ↓
Remote Control page
```

If a saved TV exists but is temporarily unavailable, do not automatically forget the saved TV or pairing authorization.

---

## Remote Screen — Top Row

The top of the Remote Control page should contain:

- [ ] Power Off
- [ ] Remote Control title/heading where appropriate
- [ ] Disconnect

The exact positioning should follow the visual balance of the reference image.

### Power Off

Add a new **Power Off** control.

Requirements:

- [ ] Determine and use the verified Samsung Q80R power-off command.
- [ ] Do not guess an unsupported Samsung command.
- [ ] Test the command against the physical Q80R.
- [ ] Power Off should be visually distinct but consistent with the remote design.

This milestone requires **Power Off**, not Wake/Power On.

Power-on functionality remains outside the scope of this milestone unless it already exists and can be retained without additional work.

### Disconnect

Move Disconnect from the existing configuration area onto the Remote Control page.

Disconnect must terminate the current remote connection without automatically forgetting the saved TV or pairing authorization.

The user should therefore be able to reconnect later without re-pairing.

---

## Primary Remote Controls

Retain the existing primary controls and their existing working Samsung command behaviour.

The controls should remain in approximately the same logical order as the current application, but their visual design and layout should be completely revamped to follow the reference image.

Primary controls include:

- Source
- Menu / Settings
- Guide
- Exit
- Home
- Up
- Down
- Left
- Right
- OK
- Back
- Volume -
- Mute
- Volume +
- Channel -
- Previous Channel
- Channel +

Do not regress existing working commands while redesigning the UI.

---

## Navigation Pad — Primary Visual Feature

The directional navigation control is the **most important visual element on the new Remote Control page**.

The following controls:

- Up
- Down
- Left
- Right
- OK

must be combined into a large unified navigation pad based closely on the reference image:

```text
C:\junk\Screenshot_20261002_121607.png
```

This control should occupy a substantial portion of the available Remote Control screen.

It should visually resemble a physical remote D-pad rather than five separate Material buttons.

Conceptually:

```text
              ↑

        ←     OK     →

              ↓
```

However, visually it should be implemented as a large circular or near-circular navigation surface similar to the reference image.

Requirements:

- [ ] Large circular navigation control.
- [ ] Up touch region.
- [ ] Down touch region.
- [ ] Left touch region.
- [ ] Right touch region.
- [ ] Clearly defined central OK control.
- [ ] Large comfortable touch targets.
- [ ] Existing Samsung navigation commands retained.
- [ ] Existing haptic-feedback behaviour retained.
- [ ] Visual pressed state for each direction where practical.
- [ ] No text wrapping such as `LEF/T`, `RIG/HT` or `DO/WN`.

Prefer directional icons/arrows rather than large text labels.

The D-pad should be one of the first things the eye is drawn to when the remote opens.

---

## Volume and Channel Controls

Redesign Volume and Channel controls to resemble the reference remote.

Instead of six equally prominent independent buttons, use compact vertical controls where practical.

Conceptually:

```text
 ┌─────┐                         ┌─────┐
 │  +  │                         │  +  │
 │     │                         │     │
 │ Vol │                         │ Ch  │
 │     │                         │     │
 │  -  │                         │  -  │
 └─────┘                         └─────┘
```

Use the reference image for the actual visual direction.

Retain:

- Volume +
- Volume -
- Mute
- Channel +
- Channel -
- Previous Channel

Mute and Previous Channel may remain separate compact controls.

---

## Home / Back / Source / Menu / Guide / Exit

These controls should remain easily accessible but should no longer dominate the screen with very large white buttons.

Use compact rounded buttons similar to the reference image.

Where appropriate, use familiar icons combined with accessible content descriptions.

Functionality must remain identical to the existing working controls.

---

## Single-Screen Requirement

All primary remote controls must fit on a normal phone screen without vertical scrolling.

The user should be able to access:

- Power Off
- Disconnect
- Source
- Menu / Settings
- Guide
- Exit
- Home
- D-pad
- OK
- Back
- Volume controls
- Mute
- Channel controls
- Previous Channel
- More Controls

without scrolling.

This is a core completion requirement.

The UI should adapt reasonably to different phone dimensions, but the physical test device is the primary reference during this milestone.

Do not solve layout problems simply by making controls uncomfortably small.

---

## More Controls

The existing `MORE CONTROLS` expandable content should be replaced with a bottom overlay/panel.

A compact **More Controls** button should appear at the bottom of the main Remote Control screen.

When tapped:

1. A panel should animate upward from the bottom of the screen.
2. It should overlay the main remote rather than extending the page vertically.
3. The primary remote should remain behind the overlay.
4. The transition should feel smooth and deliberate.

Use an appropriate Compose component/animation such as a modal bottom sheet or equivalent implementation.

---

## More Controls Overlay

The overlay contains the less frequently used controls currently displayed by `MORE CONTROLS`.

Retain the existing working controls.

### Number Keypad

```text
1 2 3
4 5 6
7 8 9
  0
```

### Playback

- Rewind
- Play
- Pause
- Stop
- Fast Forward

### Other

- Info
- Red
- Green
- Yellow
- Blue

The overlay should follow the same dark, polished visual language as the main remote.

Do not reproduce the current oversized white-button layout.

---

## Closing More Controls

The More Controls overlay must have an obvious way to close it.

Provide either:

- A Close button
- A Hide button
- A downward-chevron/handle
- Standard bottom-sheet swipe-down behaviour

Preferably support both an obvious visual close/hide control and normal swipe-down behaviour if the chosen Compose component supports it.

Closing the overlay should smoothly animate it back down and reveal the unchanged main remote.

---

## Visual Design Requirements

The new Remote Control page should use:

- Dark background
- Compact rounded controls
- Strong visual hierarchy
- Consistent spacing
- Modern iconography
- Subtle elevation/shadows where useful
- Clear pressed states
- Existing haptic feedback
- Accessible touch targets
- Minimal unnecessary text
- No oversized default Material buttons

The reference image uses a bright accent colour for its primary navigation area.

Use the reference image as the visual guide for accent treatment while still maintaining good Android accessibility and contrast.

Do not blindly copy branding or unrelated elements from the reference image.

The goal is to capture the **quality, proportions, layout and physical-remote feel**.

---

## Existing Functionality Must Be Preserved

This is primarily a UI/navigation milestone.

Do not unnecessarily rewrite the Samsung protocol implementation.

The following existing functionality must continue working:

- [ ] TV discovery
- [ ] Saved TV/IP
- [ ] Samsung pairing persistence
- [ ] Automatic/reusable authorization
- [ ] Connect
- [ ] Disconnect
- [ ] Haptic feedback
- [ ] Source
- [ ] Menu / Settings
- [ ] Guide
- [ ] Exit
- [ ] Home
- [ ] Navigation
- [ ] OK
- [ ] Back
- [ ] Volume
- [ ] Mute
- [ ] Channel controls
- [ ] Previous Channel
- [ ] Number keypad
- [ ] Playback controls
- [ ] Info
- [ ] Colour controls

Do not cause the Samsung TV to begin repeatedly displaying authorization prompts as a side effect of this refactor.

---

## Implementation Approach

Before changing code:

1. Inspect the primary reference image:

```text
C:\junk\Screenshot_20261002_121607.png
```

2. Review the existing Compose screens and navigation/state architecture.
3. Identify which existing components and command handlers can be reused.
4. Produce a short implementation plan.
5. Preserve existing Samsung/network behaviour unless a change is genuinely required by this milestone.

Then implement the UI incrementally.

Do not replace working Samsung protocol code simply to accommodate the new layout.

---

## Physical Test Procedure

### Startup With Saved TV

1. Pair/connect successfully with the Samsung Q80R.
2. Close the application completely.
3. Reopen it.
4. Confirm the saved TV/IP is restored.
5. Confirm the app goes directly to the Remote Control page.
6. Confirm it reconnects without another Samsung `Allow` prompt.

### Main Remote Layout

7. Confirm the complete primary remote fits on one screen.
8. Confirm no vertical scrolling is necessary.
9. Confirm the navigation pad is the dominant central control.
10. Confirm all labels/icons render correctly without wrapping or clipping.
11. Compare the finished Remote Control screen directly with:

```text
C:\junk\Screenshot_20261002_121607.png
```

12. Confirm that the overall visual appearance, proportions and physical-remote feel are reasonably close to the reference.

### Remote Commands

13. Test every visible primary remote control against the physical Q80R.
14. Confirm haptic feedback still works.
15. Test the new Power Off button.

### TV Setup / Settings Page

16. Navigate to the TV Setup / Settings page.
17. Confirm Scan for TVs still works.
18. Confirm Haptic Feedback can be changed.
19. Confirm Forget Saved TV behaves correctly.

### More Controls

20. Tap More Controls.
21. Confirm the overlay animates upward from the bottom.
22. Confirm the underlying main remote remains behind it.
23. Test number controls.
24. Test playback controls.
25. Test Info and colour controls.
26. Close/hide the overlay.
27. Confirm it animates smoothly downward.

### Disconnect

28. Tap Disconnect.
29. Confirm the TV connection closes.
30. Confirm the saved TV and pairing authorization are not forgotten.
31. Reconnect.
32. Confirm another Samsung `Allow` prompt is not required.

---

## Completion Criteria

This milestone is complete only when:

- [ ] The application has separate TV Setup / Settings and Remote Control pages.
- [ ] A previously paired TV causes startup to proceed directly to the Remote Control page.
- [ ] Saved Samsung pairing authorization is reused.
- [ ] The main Remote Control page closely follows the visual direction of `C:\junk\Screenshot_20261002_121607.png`.
- [ ] The D-pad/OK control is the dominant visual feature.
- [ ] All primary remote controls fit on one screen without scrolling.
- [ ] No control labels are clipped or awkwardly wrapped.
- [ ] More Controls opens as an animated bottom overlay.
- [ ] More Controls can be easily hidden/closed.
- [ ] Existing remote commands continue working.
- [ ] Scan for TVs continues working.
- [ ] Forget Saved TV continues working.
- [ ] Haptic Feedback continues working.
- [ ] Disconnect does not forget pairing.
- [ ] The new Power Off control successfully turns off the physical Samsung Q80R.
- [ ] Normal app startup/reconnection does not produce unnecessary Samsung authorization prompts.
- [ ] Project builds successfully.
- [ ] Relevant UI/architecture changes are recorded in `STATUS.md`.

---

### Stop Point

After successful physical verification:

1. Update `STATUS.md`.
2. Record the new two-page navigation structure.
3. Record the verified Power Off behaviour.
4. Record any important responsive-layout decisions.
5. Mark this milestone complete.
6. Stop.

Do not automatically continue to another milestone without user approval.

---

## Milestone 9 — TV app shortcuts

### Goal

Optionally launch installed Samsung TV apps directly.

### Research first

- [ ] Verify Q80R app discovery/launch support.
- [ ] Determine reliable app identifiers from the target TV or trustworthy sources.
- [ ] Do not assume IDs from other Samsung models are valid.

### Possible shortcuts

- [ ] Netflix
- [ ] YouTube
- [ ] Prime Video
- [ ] ABC iview
- [ ] SBS On Demand
- [ ] 9Now
- [ ] 7plus
- [ ] 10
- [ ] Other installed apps where discoverable

### Completion criteria

Only shortcuts proven reliable on the Q80R are exposed.

---

# Deferred backlog

These are deliberately outside the current milestones unless the user reprioritizes them:

- [ ] Power-on/Wake-on-LAN investigation
- [ ] Multiple TVs
- [ ] Custom remote layouts
- [ ] Favourite channels
- [ ] Favourite apps
- [ ] Home-screen widget
- [ ] Quick Settings tile
- [ ] Voice control
- [ ] Wear OS
- [ ] Macros
- [ ] LG support
- [ ] Android/Google TV support
- [ ] Roku support

# Current instruction

Start at the milestone identified in `STATUS.md`.

Never skip a milestone's explicit user-verification stop point.
