# Samsung TV Remote — Project Status

Last updated: 2026-10-02

## Current milestone

**Milestone 9 — TV app shortcuts**

Status: Milestones 5, 6, 7, and 8 physically verified by the user; Milestone 9 implementation is in place and awaiting physical Q80R verification.

## Project objective

Build a native Android/Kotlin, ad-free Samsung TV remote for a Samsung Q80R that works directly over the local home network.

## Target device

- TV: Samsung Q80R
- Platform: Samsung/Tizen
- TV IP address: TBD
- Android phone: physical test device available
- Network requirement: phone and TV must be on the same home LAN

Do not put public IP addresses, pairing tokens, passwords, or other secrets in this file.

## Agreed technology

- Kotlin
- Jetpack Compose
- Material 3
- Kotlin Coroutines
- Android Studio-compatible project
- Local-network TV control
- No cloud backend
- No ads
- No subscriptions
- No analytics/tracking

## Completed

- Milestone 1 was tested successfully and confirmed by the user on 2026-10-01.
- Milestone 2 was physically tested successfully and confirmed by the user on 2026-10-02.
- Milestone 3 was physically tested successfully and confirmed by the user on 2026-10-02.
- Milestone 4 was physically tested successfully and confirmed by the user on 2026-10-02.
- Milestone 5 was physically tested successfully and confirmed by the user on 2026-10-02.
- Milestone 6 was physically tested successfully and confirmed by the user on 2026-10-02.
- Milestone 7 was physically tested successfully and confirmed by the user on 2026-10-02.
- Milestone 8 was physically tested successfully and confirmed by the user on 2026-10-02.
- Android/Kotlin project configured with Jetpack Compose and Material 3.
- Basic package structure established for the app, UI, and theme.
- Initial screen implemented with a heading, editable TV IP field, Connect button, disconnected status, and disabled `VOLUME +` placeholder.
- Connect becomes enabled when an address is entered.
- Milestone 1 Compose UI test added.
- Manual IPv4 configuration with validation.
- Samsung connection isolated behind `TvRemoteClient` with explicit connection states.
- Candidate secure Samsung WebSocket client implemented for port 8002.
- First-time approval state, authorization rejection, disconnect, and useful network errors implemented.
- Pairing tokens are encrypted with an Android Keystore AES/GCM key and stored in private app preferences.
- Stored tokens are reused with the stable app identity `Rogermote`.
- Pairing data is excluded from cloud backup and device transfer because its encryption key is device-bound.
- Ordinary connection failures preserve the stored token.
- A rejected stored token is cleared and retried once without a token to request fresh TV approval; rejection during that fresh attempt is shown to the user without looping.
- `VOLUME +` sends the candidate Samsung `KEY_VOLUP` remote-control message only while connected.
- Development logs contain event/state information but never token values or full protocol responses.
- Generic Milestone 3 command model and Samsung-specific command mapper added.
- Milestone 4 interface command candidates added for Source, Menu/Settings, Exit, and Guide.
- Milestone 5 remote layout and secondary-control command candidates added.
- Milestone 5 channel, number-pad, playback, info, and color controls added behind an expandable section.
- Milestone 6 persistent TV configuration added for the manually selected TV IP address.
- The app now restores the saved TV address and automatically reconnects on later launches.
- A `FORGET SAVED TV` action disconnects, clears the saved address, and removes its encrypted pairing token.
- Milestone 7 SSDP/UPnP discovery added with a bounded local-network scan.
- Discovered Samsung TVs show a friendly name when their UPnP description provides one, plus IP address and model where available.
- Discovery results can be selected to populate the existing manual connection field; manual IP entry remains available as a fallback.
- Milestone 8 persisted haptic feedback preference added, enabled by default and independently resettable from TV configuration.
- Volume, channel, and directional controls support controlled press-and-hold repeat with a delay and capped interval.
- App foreground/background lifecycle handling now disconnects stale sessions and reconnects saved TVs when returning.
- Default-network callbacks now surface network loss and schedule a reconnect when the network returns.
- User-facing network errors, unavailable-network messaging, and accessible control semantics were reviewed and retained.
- Milestone 8.5 separates TV Setup / Settings from the everyday Remote Control page.
- The Remote Control page now uses a dark compact layout with a unified circular D-pad, vertical volume/channel controls, and a fixed no-scroll primary surface.
- More Controls now opens as an animated modal bottom sheet containing the keypad, playback, info, and colour controls.
- Power Off is exposed through the new generic command model using the Samsung `KEY_POWER` candidate.
- Milestone 9 app shortcut plumbing added without hard-coded application names or IDs.
- The authenticated Samsung WebSocket requests installed applications using the candidate `ed.installedApp.get` event.
- TV-returned application names, IDs, and icon references are parsed into `TvApplication` models.
- The reserved shortcut row now uses a horizontally scrolling `LazyRow` populated from discovered applications.
- Tapping a discovered application sends the candidate `ed.apps.launch` event using its TV-returned application ID.
- A generic fallback play icon is shown until the Q80R's icon reference format is physically confirmed and icon retrieval is implemented.

## Verified Samsung protocol facts

The user confirmed the Samsung Q80R accepts the implemented secure WebSocket connection, pairing flow, token reuse, `KEY_VOLUP` command, all Milestone 3 controls, the Milestone 4 interface controls, and the Milestone 5 controls.

Research supports `wss://<TV_IP>:8002/api/v2/channels/samsung.remote.control`, a Base64-encoded app name, `ms.channel.connect` token delivery, token reuse through the URL, and the Samsung remote key format. The user confirmed the Milestone 4 mappings `KEY_SOURCE`, `KEY_MENU`, `KEY_EXIT`, and `KEY_GUIDE` on the target Q80R. Milestone 5 secondary mappings were confirmed by the user.

## Physical-device verification

Samsung discovery uses standard SSDP/UPnP multicast at `239.255.255.250:1900`, listens for both search responses and advertisements, filters Samsung device signatures, and reads the advertised local description URL on a best-effort basis for device identity.

- Automated Compose UI test passed on a connected Pixel 6 running Android 17.
- Debug app installed and cold-launched successfully on that device.
- User confirmed Milestone 1 successful.
- Android Keystore token persistence/encryption test passed on the Pixel 6.
- Samsung Q80R pairing, volume command, app-restart persistence, TV-restart persistence, and repeated reconnection were confirmed successful by the user on 2026-10-02.
- Samsung Q80R volume, mute, directional navigation, OK/Enter, Back, and Home were confirmed successful by the user on 2026-10-02.
- Samsung Q80R Source, Menu/Settings, Exit, and Guide were confirmed successful by the user on 2026-10-02.
- Samsung Q80R Milestone 5 channel, keypad, playback, info, and color controls were confirmed successful by the user on 2026-10-02.
- Samsung Q80R Milestone 6 automatic reconnect and configuration reset were confirmed successful by the user on 2026-10-02.
- Samsung Q80R SSDP discovery, result selection, connection, and manual-IP fallback were confirmed successful by the user on 2026-10-02.

## Build/test results

- 2026-10-01: `assembleDebug`, `testDebugUnitTest`, and `assembleDebugAndroidTest` passed.
- 2026-10-01: `connectedDebugAndroidTest` passed (1 test) on Pixel 6 / Android 17.
- 2026-10-01: `installDebug` succeeded and `MainActivity` cold launch returned `Status: ok`.
- 2026-10-01: Revised Milestone 2 `assembleDebug`, 7 JVM tests, and Android test APK build passed.
- 2026-10-01: 2 connected Android tests passed on Pixel 6 / Android 17 (Compose UI and encrypted token-store persistence).
- 2026-10-01: Revised Milestone 2 debug APK installed and cold-launched successfully.
- Packaging reported the existing AndroidX native-library strip notice for `libandroidx.graphics.path.so`; it was packaged as-is and did not fail the build.
- Milestone 2 physical verification is complete; no Samsung key beyond `KEY_VOLUP` has been confirmed on the Q80R yet.
- 2026-10-02: Milestone 3 `assembleDebug`, `testDebugUnitTest`, and Android test APK build passed.
- 2026-10-02: 2 connected Android tests passed on Pixel 6 / Android 17.
- 2026-10-02: Milestone 3 debug APK installed and `MainActivity` cold-launched successfully.
- 2026-10-02: Milestone 3 physical control verification was confirmed successful by the user.
- 2026-10-02: Milestone 4 `assembleDebug`, `testDebugUnitTest`, and Android test APK build passed.
- 2026-10-02: 2 connected Android tests passed on Pixel 6 / Android 17.
- 2026-10-02: Milestone 4 debug APK installed and `MainActivity` cold-launched successfully.
- 2026-10-02: Milestone 4 physical interface-control verification was confirmed successful by the user.
- 2026-10-02: Milestone 5 `assembleDebug`, JVM tests, and Android test APK build passed.
- 2026-10-02: 2 connected Android tests passed on Pixel 6 / Android 17.
- 2026-10-02: Milestone 5 debug APK installed and `MainActivity` cold-launched successfully.
- 2026-10-02: Milestone 6 `assembleDebug`, JVM tests, and Android test APK build passed.
- 2026-10-02: 3 connected Android tests passed on Pixel 6 / Android 17, including TV configuration persistence and clearing.
- 2026-10-02: Milestone 6 debug APK installed and `MainActivity` cold-launched successfully.
- 2026-10-02: Milestone 7 `assembleDebug`, JVM tests, and Android test APK build passed.
- 2026-10-02: 3 connected Android tests passed on Pixel 6 / Android 17.
- 2026-10-02: Milestone 7 debug APK installed and `MainActivity` cold-launched successfully.
- 2026-10-02: Milestone 8.5 `assembleDebug`, JVM tests, and Android test APK build passed.
- 2026-10-02: 4 connected Android tests passed on Pixel 6 / Android 17 after the final UI polish pass.
- 2026-10-02: Final Milestone 8.5 debug APK installed successfully on the Pixel 6; an earlier build also cold-launched `MainActivity` successfully.
- 2026-10-02: Milestone 8 `assembleDebug`, JVM tests, and Android test APK build passed.
- 2026-10-02: Connected Android test attempt was blocked because the Pixel 6 disconnected from ADB; `adb devices` reported no connected devices.
- 2026-10-03: Milestone 9 Samsung app protocol tests passed, including discovery parsing and launch-message serialization.
- 2026-10-03: Milestone 9 `testDebugUnitTest` and `assembleDebug` passed.
- 2026-10-03: Corrected the app-launch payload to send structured `appId`, `action_type`, and `metaTag` data after physical testing showed no launch response; physical retest is pending.
- 2026-10-03: Added sequential `ed.apps.icon` requests using each discovered icon path, in-memory decoded icon state, and a fallback icon when the TV response is unavailable or unusable.
- 2026-10-03: Expanded icon handling to accept direct image data, nested response fields, and TV-local HTTP paths/URLs; icon retrieval remains pending physical Q80R confirmation.
- 2026-10-03: Added bundled native brand icons sourced from the public Simple Icons catalog for YouTube, Netflix, Apple TV, Spotify, Plex, Paramount+, Max, Twitch, and Crunchyroll. Discovered apps use a matching bundled icon first, then TV-provided image data, then the generic fallback.
- 2026-10-03: Bundled-icon `testDebugUnitTest` and `assembleDebug` passed.
- 2026-10-03: Added bundled icons and matching for Prime Video, Disney+, ABC iview, 7plus, 10 play, Kayo, and Internet/browser entries using public Iconify assets plus a native globe fallback.
- 2026-10-03: Expanded bundled-icon `testDebugUnitTest` and `assembleDebug` passed.
- 2026-10-03: Added bundled icons and matching for Stan, Binge, 9Now, NFL, Foxtel, Google, Google TV, Tubi, Telstra, and Calm. BritBox, main SBS, and a clearly branded Universal TV asset remain unavailable in the selected public catalog.
- 2026-10-03: Added sourced bundled icons and matching for SBS, current Network 10, Universal TV, and BritBox.

## Important decisions

1. Samsung Q80R is the first and only required TV target for the initial implementation.
2. The app should eventually allow access to the TV's Home, Settings, Source, app selection, Guide, and other TV UI through normal remote navigation.
3. Development is milestone-based.
4. The first real protocol success criterion is `VOLUME +` changing the volume on the physical TV.
5. Later-brand support is a future possibility, not a reason to over-engineer the initial version.
6. Important project state should be kept in this file so a new Codex session can continue without relying on old chat context.
7. The pairing token is keyed by the manually entered TV IP for this single-TV proof of concept.
8. A stable Base64-encoded `Rogermote` app name is reused for every Samsung connection.
9. The Q80R's expected self-signed TLS certificate is accepted only by the Samsung-TV-specific HTTP client and only for the exact entered IP; global TLS verification is unchanged.
10. Milestone 2 invalid-token recovery performs at most one tokenless re-pairing attempt.
11. Milestone 3 exposes only generic `RemoteCommand` values to the UI; Samsung key names remain inside the Samsung adapter.
12. `MENU / SETTINGS` is the initial Settings path; no undocumented dedicated Settings key is assumed.
13. Less-used Milestone 5 controls live behind an explicit expandable section to keep common remote actions compact.
14. Non-secret TV configuration is stored separately from the encrypted pairing token; the configured protocol remains the Samsung secure WebSocket adapter's fixed port.
15. Milestone 7 uses bounded SSDP/UPnP multicast rather than subnet scanning; discovery never sends TV data outside the local network.
16. Haptics are stored with non-secret local configuration but are not cleared when the saved TV is forgotten.
17. Remote hold-repeat is limited to volume, channel, and directional navigation to avoid repeating one-shot commands.
18. Milestone 8.5 keeps setup/discovery actions off the primary remote page and preserves the existing generic command callbacks.
19. Power Off is isolated as a Samsung adapter mapping so it can be physically validated without coupling the UI to protocol strings.

## Known issues

- Samsung's consumer remote-control WebSocket is undocumented; the candidate endpoint and behavior require Q80R confirmation.
- Secure port 8002 is the only protocol attempted in Milestone 2. No cleartext fallback is implemented without evidence it is needed.
- SSDP discovery depends on the phone and TV sharing a LAN that permits multicast; some routers isolate Wi-Fi clients or suppress multicast advertisements.
- Milestone 8 haptics, hold-repeat, network loss/recovery, and foreground/background behavior were implemented and included in the user-confirmed Milestone 8 verification.
- Milestone 8.5 visual layout, two-page startup behavior, bottom-sheet controls, and Power Off require physical Q80R verification before this milestone can be marked complete.
- Milestone 9's installed-app response format, icon retrieval path, and launch behavior are not yet physically verified on the Q80R.

## Next action

On the physical Q80R, verify the new Remote Control layout, Setup page navigation, all existing commands, and the new Power Off control. Then confirm whether installed-app discovery returns names, IDs, and usable icon references, and test launching one discovered application from the reserved row.
