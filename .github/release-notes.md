Chats with the phone's own keyboard instead of the LightOS one: every text
field — the composer, search, the login fields, the recovery key — is a normal
Android text field, so whichever IME you have installed and enabled is the
keyboard you type with. Settings → **Device Keyboard** switches back to the
LightOS keys.

**Installing**

- An R8-minified **release** APK signed with the light-sdk development key.
  Same sideload route as upstream: Developer options → External tools → **All
  tools**, then `adb install -r` (or Obtainium, tracking this repo).
- The keyboard itself still has to be installed and enabled over ADB — LightOS
  ships no IME and no input-settings screen. Commands are in
  [docs/DEVICE-KEYBOARD.md](https://github.com/KEZO555/Messages/blob/main/docs/DEVICE-KEYBOARD.md).
- **With no IME enabled, no keyboard appears.** That is the one failure mode of
  this build, and Settings → Device Keyboard is the way out of it.

**Note on secrets**

The LightOS keyboard is drawn inside the app and sees nothing else; a system IME
is a separate app that now sees everything you type here. The password/token and
recovery-key fields ask the IME to drop suggestions and skip its dictionary,
which well-behaved keyboards honour — prefer an offline, open-source one, or
turn Device Keyboard off while signing in.

Built from this tag by GitHub Actions against
[fenleon/light-sdk](https://github.com/fenleon/light-sdk). Upstream:
[fenleon/chats](https://github.com/fenleon/chats).
