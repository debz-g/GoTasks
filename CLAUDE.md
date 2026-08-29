# CLAUDE.md

Guidance for Claude Code when working in this repository. See [README.md](README.md) for what the
app is, its architecture, and Google Cloud setup. This file covers the things that aren't obvious
from reading the code, and the invariants that are easy to break.

## Commands

```bash
./gradlew :app:assembleDebug        # build
./gradlew :app:testDebugUnitTest    # unit tests
```

Gradle's summary line says `BUILD SUCCESSFUL` even when tests fail, so **check the XML results**
rather than trusting it:

```bash
python3 -c "
import glob, xml.etree.ElementTree as ET
for p in sorted(glob.glob('app/build/test-results/testDebugUnitTest/*.xml')):
    r = ET.parse(p).getroot()
    print(r.get('name'), r.get('tests'), 'tests,', r.get('failures'), 'failures')"
```

**Do not install or launch the app yourself.** Report that the build passed and ask the user to hit
Run in Android Studio. Auth tokens are in-memory by design, so a fresh install forces them to sign
in again. Once they confirm it's running, `android screen capture`, `android layout -p` and
`adb shell input` are fine for inspecting it.

## Secrets — the repo is public

`local.properties` holds `GOTASKS_WEB_CLIENT_ID`, injected into `BuildConfig` at build time. It is
gitignored along with `keystore.properties`, `*.jks`, `*.keystore`, `google-services.json`.

Never `git add -A` without reading the staged list afterwards, and grep the staged diff for
`apps.googleusercontent.com`, `AIza`, `PRIVATE KEY`, `storePassword` before committing.

Pushing needs the personal GitHub account; the CLI usually has the work one active. Switch, push,
then **switch back** so their setup is left as found:

```bash
gh auth switch --user debz-g && git push; gh auth switch --user debz-artium
```

## Google Tasks API constraints

These are API limits, not bugs, and they've each been rediscovered the hard way:

- **`due` is date-only.** The API discards the time and can neither read nor write it. Times set in
  the app are local reminders (`localReminderTime`) and never reach Google.
- **No recurrence field**, so repeating tasks aren't offered at all.
- **No idempotency key.** A CREATE re-sent after its response was lost creates a duplicate — hence
  the concurrency rules below.
- **One level of subtask nesting.** GoTasks renders existing subtasks but doesn't create them.
- The API surface is mirrored by hand in `TasksApiService`; the official SDK isn't used.

## Sync invariants

Breaking any of these produces duplicated or silently-lost data:

1. **Local-only fields must be merged from the cached row.** `TaskDto.toEntity(taskListId, cached)`
   carries over `isStarred`, `localReminderTime` and friends. A response says nothing about them, so
   anything not merged resets on the next pull. Add new local-only columns to that merge — don't
   thread another parameter through call sites.
2. **All syncing goes through `SyncEngine.sync()`, which holds a `Mutex`.** Syncs start from two
   independent places: `SyncWorker` and the Refresh button, which calls the engine directly without
   touching WorkManager. Run concurrently, both drains read the same pending CREATE and both POST
   it. Never add a third path that bypasses the mutex.
3. **Push enqueues with `ExistingWorkPolicy.KEEP`, never `REPLACE`.** REPLACE cancels a *running*
   worker; cancelled between "POST succeeded" and "outbox row deleted", the CREATE stays queued and
   the next run duplicates it.
4. **Push before pull**, so local changes reach the server before server state is folded back in.
5. **Skip-if-dirty**: a pull never overwrites a row with unpushed changes (`getAllPendingEntityIds`).
6. **The outbox drains serially, one op at a time**, re-querying each iteration. That ordering is
   what makes temp-id reconciliation safe — a CREATE always completes and remaps its `local_<uuid>`
   before any later op touching that entity.
7. **Ids needing rewrites live in columns, not `payloadJson`.** Reconciliation can rewrite a column;
   it can't reach inside an opaque JSON blob.
8. **PATCH bodies use `TaskUpdateDto`.** `Json` is configured `explicitNulls = false`, so a Kotlin
   null is *omitted* and reads as "leave unchanged". `due` is typed `JsonElement` so `JsonNull`
   survives serialisation and actually clears the field.

## Database

Room with `fallbackToDestructiveMigration` — it's a rebuildable cache, so **any** schema change just
needs a version bump in `AppDatabase`. Forgetting the bump crashes at runtime. Warn the user that
the local cache wipes and re-pulls on next launch.

## Conventions

- **MVI per feature**: `State` / `Intent` / `ViewModel` / `Screen`. Screens dispatch intents only.
- **Koin** for DI, modules under `di/`. New singletons go in the matching module; ViewModels use
  `viewModel { }` from `org.koin.core.module.dsl`.
- **Compose only.** Two custom primitives exist because the Material3 versions can't be made flush
  with the sheet margin: `FlushTextField` (M3 `TextField` has unoverridable internal padding on this
  API level) and `SheetEdgeIconButton` (`IconButton` centres its glyph in a 48dp touch target).
  Shared sheet styling lives in `SheetDefaults.kt` — change padding there, not per-sheet.
- **Theme**: true black `#000000`, accent coral `#E14039`, surfaces `#1C1C1C`/`#262626`, Poppins.
  Hardcoded dark scheme; no light theme, no theme picker.
- Formatting is 2-space indent, ~140 columns, trailing commas. Match surrounding code.
- Comments explain *why*, especially where something looks odd but is deliberate. Several
  workarounds here are non-obvious and were expensive to find.

## Testing

Unit tests cover pure logic — the date parser, label formatting, subtask ordering. `DateTimeParser`
is deliberately Android-free so it stays directly testable. There are no instrumentation tests; UI
is verified by the user running it.

When fixing a bug caused by a wrong assumption, add a test that encodes the correct one.
