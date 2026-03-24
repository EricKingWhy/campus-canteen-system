# Thread Handoff Index

## How To Use
1. Finish a thread, then create one file in `docs/handoffs/` from the template.
2. Update this index with latest status and the new handoff file path.
3. New thread must read this file first, then read the latest 1-2 handoff files.

## Current Project Snapshot
- Last updated: 2026-03-24
- Active focus: miniapp real-device bugfix and UI polish
- Must-run checks before claiming done: `npm run build:mp-weixin`

## Latest Handoffs
- (add newest first) `docs/handoffs/2026-03-24_XXXX_topic.md`

## Open Risks / TODO
- (example) Real-device only issues may not reproduce in devtools.
- (example) Verify same fix path on iOS/Android WeChat.

## Quick Start For New Thread
1. Read this file.
2. Read latest handoff(s) in `docs/handoffs/`.
3. Run:
   - `git status`
   - `git diff --name-only`
4. Continue from the listed TODO and validation gaps.
