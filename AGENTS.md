# Codex Project Instructions

## Project

This repository contains a native Android/Kotlin remote-control application for a Samsung Q80R television.

The immediate goal is a reliable, simple, ad-free remote that communicates directly with the TV over the local network. Samsung support comes first. The architecture may allow other TV brands later, but do not add speculative complexity for them now.

## Required reading before significant work

Before making significant changes:

1. Read `SPEC.md`.
2. Read `PLAN.md`.
3. Read `STATUS.md`.
4. Identify the current milestone and its completion criteria.
5. Work only on the current milestone unless the user explicitly asks otherwise.

Treat these files as the durable project context. Do not rely on previous chat history being available.

## Development stack

Use:

- Kotlin
- Android Studio-compatible project structure
- Jetpack Compose
- Material 3
- Kotlin Coroutines
- StateFlow where appropriate
- Gradle Kotlin DSL where practical

Prefer current stable Android/Kotlin APIs and straightforward implementations.

## Core constraints

- No advertisements.
- No subscriptions.
- No analytics or tracking.
- No cloud backend.
- No account system.
- TV control must work locally on the home network.
- Do not transmit the TV IP address, pairing token, device identifiers, or usage data to external services.
- Avoid unnecessary third-party dependencies.
- Keep networking/protocol code out of Composables.
- Do not scatter raw Samsung key strings throughout UI code.
- Do not hard-code the user's TV IP address in source code.
- Do not silently change requirements in `SPEC.md`.

## Samsung protocol rule

Do not assume undocumented Samsung behaviour is guaranteed.

Before depending on a Samsung-specific endpoint, WebSocket message, authentication flow, token behaviour, key code, app-launch mechanism, discovery mechanism, or Wake-on-LAN behaviour:

1. Verify it against reliable documentation, observed TV responses, or a controlled test.
2. Keep model/firmware-specific behaviour isolated in the Samsung implementation.
3. Record important confirmed findings in `STATUS.md`.

The target TV is a Samsung Q80R, but firmware differences may exist.

## Implementation process

Work milestone by milestone.

For each milestone:

1. Review its scope and completion criteria in `PLAN.md`.
2. Implement the smallest clean solution that satisfies that milestone.
3. Build the project.
4. Fix build errors before proceeding.
5. Run relevant automated/unit tests where practical.
6. Report any physical-TV or phone testing the user must perform.
7. Do not mark a physical-device requirement complete until the user confirms it.
8. Update `STATUS.md`.
9. Stop at milestone boundaries when `PLAN.md` says user verification is required.

Do not implement later milestones merely because they are easy to add.

## STATUS.md maintenance

After meaningful work, update `STATUS.md` with:

- Current milestone
- Completed work
- Build/test results
- Physical-device test results confirmed by the user
- Samsung protocol facts that have actually been verified
- Important architectural decisions
- Known issues
- Next action

Keep `STATUS.md` concise enough that a new Codex session can quickly understand the project.

Never put secrets or sensitive pairing tokens in `STATUS.md`.

## Coding style

Prefer:

- Small focused classes/functions
- Clear naming
- Immutable UI state where practical
- State hoisting in Compose
- Coroutines for asynchronous work
- Explicit connection state
- Structured error handling
- Dependency boundaries that are easy to test
- Comments that explain non-obvious protocol behaviour, not obvious syntax

Avoid:

- Huge classes
- Global mutable state
- Blocking network work on the main thread
- Networking logic directly inside Composables
- Premature abstraction
- Unnecessary frameworks
- Large speculative refactors unrelated to the current milestone

## Build discipline

Before declaring a coding milestone complete:

- The project must compile.
- Relevant tests must pass.
- New warnings/errors should be investigated.
- User-visible failures should produce understandable messages rather than crashes.

If the environment cannot run a required build or test, state exactly what could not be verified.

## Git discipline

Keep changes focused on the current milestone. Do not rewrite unrelated files.

If Git is available, inspect the diff before finishing a milestone and summarize the meaningful changes.

Do not commit, push, rewrite history, or create remote resources unless the user asks.

## First-run instruction

If `STATUS.md` says the project has not started, begin with Milestone 1 only.

After Milestone 1 builds successfully, update `STATUS.md` and stop for user verification before proceeding to Milestone 2 unless the user explicitly authorizes continuing.
