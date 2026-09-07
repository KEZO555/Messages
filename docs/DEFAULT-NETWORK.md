# Default network

Upstream the chat list always opens on **All** — every bridged network mixed
into one list — and the Networks panel (the ☰ button, bottom right) filters it
for the rest of the session. If you mostly use one network, that is a filter you
re-apply every time the tool starts.

**Settings → Default Network** picks the network the list opens on. The panel is
the same one the chat list uses: **All**, then every network the companion
tagged your rooms with, with the current choice underlined. "All" is upstream's
behaviour and the default.

## How it behaves

- The saved default is applied **once per session**, to the first chat list.
  Switching networks from the Networks panel afterwards sticks — returning from
  a thread, or from Settings, never snaps back to the default.
- A panel choice is a deliberate one and outranks the default for that session;
  it does not change what is saved. Settings is the only place the default
  changes.
- If the saved network no longer exists — the bridge was removed, or a different
  account signed in — the first room list clears the filter back to All rather
  than opening an empty list under a network that isn't there. A network you
  picked in the panel is never cleared this way.
- The picker lists the networks of the rooms currently loaded, plus whatever is
  already saved, so your choice is always visible even before the room list has
  arrived.

## Where it lives

| File | Change |
| --- | --- |
| `ChatSettings.kt` | `defaultNetwork` preference (`chats.default_network`); null = All, stored by removing the key. |
| `screens/SettingsScreen.kt` | The **Default Network** row, and the network census the picker needs. |
| `screens/ChatListScreen.kt` | `applyDefaultNetwork` (once per session), `selectNetwork` for panel picks, and the stale-default clean-up when the rooms arrive. |
| `screens/AccountsScreen.kt` | Unchanged — the existing Networks panel is reused as the picker. |
