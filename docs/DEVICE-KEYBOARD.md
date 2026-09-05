# Device keyboard

Upstream Chats types with the **LightOS keyboard**: every editor is the SDK's
`LightTextInputEditor`, which draws the closed `light-keyboard` component inside
the app's own window. It is not an Android input method, so an IME you install
on the phone can never appear in Chats — the LP3 keys are the only keys.

This fork adds a second editor that is a plain Compose text field. Focusing it
opens the **system IME**, so a third-party keyboard types into Chats exactly as
it does into any other Android app.

It is on by default. **Settings → Device Keyboard** turns it off and puts the
LightOS keyboard back; the choice is stored with the tool's other preferences
and survives restarts.

## Getting a keyboard onto a Light Phone III

LightOS ships no IME and no "Languages & input" settings screen, so the keyboard
is installed and enabled over ADB. Same prerequisites as installing Chats
itself: USB debugging on, Developer options → External tools → **All tools**.

```bash
adb install -r your-keyboard.apk

# The id of every input method the phone now knows about:
adb shell ime list -a -s

# Enable it, then make it the default (ids look like com.example.kb/.LatinIME):
adb shell ime enable com.example.kb/.LatinIME
adb shell ime set com.example.kb/.LatinIME
```

`adb shell ime list -s` shows what is currently enabled, and
`adb shell ime reset` restores the system default if a keyboard misbehaves.

Any keyboard that runs on Android 14+ works — the app asks for a text field, not
for a particular IME. Offline keyboards with a small footprint (HeliBoard,
Unexpected Keyboard, OpenBoard, FlorisBoard) suit the phone best.

**If no IME is enabled, no keyboard appears** — the field takes focus and
nothing comes up. That is the one failure mode of this fork, and the reason the
LightOS keyboard is still one toggle away in Settings.

## What changed

| File | Change |
| --- | --- |
| `screens/DeviceKeyboardEditor.kt` | New. `ChatsTextInputEditor` picks the editor; `DeviceKeyboardTextInputEditor` is the system-IME one — top bar with back + title, `BasicTextField` in the middle, submit in the bottom bar (or the top-right corner for the composer). |
| `ChatSettings.kt` | New `deviceKeyboard` preference (`chats.device_keyboard`), default on. |
| `screens/SettingsScreen.kt` | New **Device Keyboard** toggle row. |
| `screens/ChatListScreen.kt` | Loads the preferences on the entry screen — search and the login editors are reachable without opening Settings or a thread. |
| `screens/ComposerScreen.kt`, `SearchScreen.kt`, `AccountScreen.kt`, `RecoveryKeyEditorScreen.kt` | The four editors call `ChatsTextInputEditor`, passing their original `LightTextInputEditor` call verbatim as the fallback. |

Both editors drive the same `TextFieldState`, so the screens' own logic —
composer drafts, the recovery key's live regrouping, the search query — is
unchanged by which keyboard is on screen.

Per-screen behaviour is carried over:

- **Composer** — multi-line, sentence capitalisation, the IME's return key
  inserts a newline (SEND stays in the top bar), draft text bottom-anchored.
- **Search** — single line, the IME's action key reads SEARCH and runs the
  search.
- **Login fields / contact search** — single line, the action key submits.
- **Recovery key** — three lines (the formatter inserts the dashes and
  newlines as you type), the action key submits the clean 48-character key.

## Secrets and the keyboard

This is the one thing the fork changes that is not a convenience. The LightOS
keyboard is drawn inside the app's own process, so nothing else ever saw what
you typed. A system IME is a separate app, and everything typed into Chats now
passes through it — including your Matrix password or access token and your
recovery key.

The password/token editor and the recovery-key editor therefore declare
`KeyboardType.Password`, which is how Android tells an IME to drop the
suggestion strip and keep the text out of its learned dictionary. Well-behaved
keyboards honour it; a hostile or cloud-syncing one is under no obligation to.
Choose the keyboard accordingly — an offline, open-source IME is the safe class
of choice — or flip **Device Keyboard** off in Settings while you sign in and
back on afterwards.

No window plumbing is involved: `LightActivity` already calls
`WindowCompat.setDecorFitsSystemWindows(window, false)` at start-up, so the IME
insets reach Compose and `Modifier.imePadding()` keeps the bottom bar — and the
composer's clear-draft X — above the keys. Tool code could not touch the window
anyway: the SDK's Gradle plugin scans every `.kt` file in the tool module and
fails the build on `LocalContext`, `android.app.*` or `android.content.Context`
imports, and on Activity casts. The editor stays inside that sandbox.

## Version

`app/lighttool.toml` carries `versionCode 70` / `versionName 0.10.1` — upstream
0.10.0 plus this change. The SDK plugin validates the name as strict
`major.minor.patch`, so the fork is not marked with a suffix; the tool id is
unchanged, so this build installs over an existing Chats.

## Building

Unchanged from upstream — the fork touches only `:app` sources. The build is a
Gradle composite: `settings.gradle.kts` includes `../light-sdk`, which must be
[fenleon/light-sdk](https://github.com/fenleon/light-sdk) (the public fork of
`lightphone/light-sdk` carrying the chat service methods, the text-editor
patches this app calls, and the dev signing key). The stock SDK will not
compile this app.

```bash
git clone https://github.com/fenleon/light-sdk light-sdk
git clone https://github.com/KEZO555/Messages chats
cd chats && ./gradlew :app:assembleDebug     # app/build/outputs/apk/debug/
```

Needs JDK 17 and the Android platform 36 / build-tools 36.

`.github/workflows/build-apk.yml` does exactly this on GitHub Actions and
uploads the APK as a build artifact, so a release needs no local Android
workspace — run it from the Actions tab (**Build APK → Run workflow**) and
download `chats-devkeyboard-debug-apk` from the finished run.
