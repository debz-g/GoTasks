# GoTasks

A custom Android client for **Google Tasks** — built because the stock Google Tasks app and widget
are hard to love, and most third-party alternatives paywall their themes and widgets.

GoTasks is deliberately *not* a standalone task manager. It reads and writes the real Google Tasks
API, so everything stays in sync with Gmail, Google Calendar, and Keep exactly as before — you just
get a nicer UI on top. Visual design takes cues from TickTick: true-black theme, coral accent,
minimal bottom sheets.

> Personal project, built for my own daily use. Feel free to fork it, but it isn't published to
> Play and isn't intended as a general-purpose product.

## Screens

| | |
|---|---|
| Sign-in | Minimal centered layout, Google sign-in via Credential Manager |
| Task list | Grouped tasks, subtask indentation, collapsible completed section |
| Add task | Bottom sheet with optional details field and star |
| Edit task | Peek sheet that expands to full screen, with an overflow menu |

## Features

- **Real Google Tasks sync** — no separate account, no separate data store
- **Multiple lists** with in-app create / rename / delete
- **Natural-language due dates** — typing "get groceries tomorrow at 5pm" sets the date, highlights
  the phrase as you type, and strips it from the saved title
- **Custom date picker** modelled on TickTick, for setting dates by hand
- **Offline-first** — Room is the source of truth; local edits queue in an outbox and push when
  connectivity returns, with incremental pulls on foreground and a 15-minute timer
- **Silent re-authorization** on launch, so you don't sign in every cold start
- **True-black theme** with Poppins, tuned for OLED

Existing subtasks sync down from Google and render nested, but GoTasks doesn't create or re-parent
them.

### Not built yet

Reminder notifications and the home-screen widget. See [Roadmap](#roadmap).

### Google Tasks API limits worth knowing

Some things simply aren't possible through the public API, so they're either local-only or absent:

| | |
|---|---|
| **Due *times*** | `due` records the date only — the API "isn't able to read or write the time that a task is due". Times set here are local reminders and won't reach Google or another device. |
| **Recurrence** | No field exists on the v1 API, so repeating tasks aren't offered. |
| **Starring** | No field either; stars are local-only. |

## Architecture

Single-module, MVI throughout.

```
com.debzg.gotasks
├── data/
│   ├── auth/        Credential Manager sign-in, Tasks-scope authorization, token handling
│   ├── local/       Room database, DAOs, outbox recorder
│   ├── remote/      Retrofit interface + DTOs for the Tasks REST API
│   ├── mapper/      DTO ↔ entity ↔ domain conversions
│   ├── repository/  Repository implementations
│   └── sync/        Push and pull stages, sync engine, WorkManager worker + scheduler
├── datetime/        Rule-based natural-language date parser (pure Kotlin, no Android deps)
├── domain/          Models and repository interfaces
├── presentation/    State / Intent / ViewModel / Screen per feature
└── ui/theme/        Colors, Poppins typography, Material3 theme
```

**Stack:** Kotlin, Jetpack Compose, Koin (DI), Room, Retrofit + kotlinx.serialization, WorkManager,
Credential Manager + Google Identity Services.

A few decisions worth calling out:

- **Hand-rolled Retrofit interface** rather than `google-api-services-tasks`. The official SDK
  brings its own auth abstractions that fight Credential Manager, and its Java-first API is
  awkward from Kotlin. The Tasks REST API is small enough to mirror directly.
- **Outbox pattern for writes.** Every mutation writes to Room optimistically *and* records a
  pending operation. A WorkManager job drains that queue one op at a time — serial ordering is what
  makes it safe to reconcile temporary local ids (`local_<uuid>`) with server-assigned ones, since
  a CREATE always completes before any later op referencing the same entity.
- **Access tokens are never persisted.** They live in memory only. On launch the app asks Play
  Services for a fresh token, which succeeds silently as long as consent still stands.
- **Skip-if-dirty on pull.** A server pull never overwrites a row that still has unpushed local
  changes.

## Setup

Requires Android Studio, JDK 17, and a device or emulator on **Android 14 (API 34)** or newer.

### 1. Google Cloud Console

The app talks to the Tasks API on your behalf, so you need your own OAuth clients.

1. Create a project at [console.cloud.google.com](https://console.cloud.google.com).
2. Enable the **Google Tasks API** (APIs & Services → Library).
3. Configure the **OAuth consent screen**:
   - User type: **External**
   - Publishing status: leave as **Testing** — no verification review needed for personal use
   - Add your own Google account under **Test users** (sign-in is blocked otherwise)
   - Add the scope `https://www.googleapis.com/auth/tasks`
4. Create an **Android** OAuth client:
   - Package name: `com.debzg.gotasks`
   - SHA-1: get your debug fingerprint with
     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
5. Create a **Web application** OAuth client. Leave the redirect URI fields empty — you only need
   its client ID, which identifies the app to Google's auth services.

On first sign-in you'll see an "unverified app" warning. That's expected for a Testing-mode app;
continue via **Advanced → Go to GoTasks**.

### 2. Local config

Add your Web client ID to `local.properties` (gitignored, never committed):

```properties
GOTASKS_WEB_CLIENT_ID=1234567890-abcdefg.apps.googleusercontent.com
```

It's injected into `BuildConfig` at build time, so no credentials live in source.

### 3. Build

```bash
./gradlew :app:assembleDebug
```

Or just hit Run in Android Studio.

## Roadmap

- [x] Project scaffold, theme, Poppins typography
- [x] Google sign-in + Tasks-scope authorization
- [x] Room cache + read-only mirror of tasks and lists
- [x] Local CRUD for tasks and lists, with outbox
- [x] Push sync with temp-id reconciliation
- [x] Pull sync — incremental `updatedMin`, periodic and foreground triggers
- [x] Natural-language due dates in quick-add, with inline highlighting
- [x] Manual date/time picker
- [ ] Reminders via local notifications
- [ ] Home-screen widget (Jetpack Glance)
- [ ] Release signing and polish

Dropped on purpose: subtask creation/re-parenting (complexity outweighed the value — display is
kept) and recurring tasks (unsupported by the API).

## License

[GPL-3.0](LICENSE)
