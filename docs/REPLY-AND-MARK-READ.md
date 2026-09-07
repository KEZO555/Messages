# Reply, and swipe to mark read

Two additions to the chat surfaces. Both ride on plumbing the SDK and the
companion already had — nothing new crosses the binder.

## Reply to a message

Long-press any message in a thread and the context window now offers **REPLY**,
above the existing actions. It opens the composer with the top bar naming who
you are answering ("Reply to Ada"; just "Reply" for your own message), and SEND
attaches an `m.in_reply_to` relation to the original.

`SendMessage.Request` already carried `replyToEventId`, and the companion
already fed it to Trixnity's `reply(event)` builder — only the tool had no way
to set it. So a reply sent here is a real Matrix reply, and a bridged network
(WhatsApp, Signal, Telegram) shows it quoting the original the way its own
client would.

**What it does not do yet:** the thread does not *draw* the quote. The SDK's
`GetMessages.Message` carries no reply fields, so a reply — yours or theirs —
renders as an ordinary message here. Showing the quoted line would need a new
field on that shared model, which lives in the SDK, not in this repo.

A reply is never saved as the room's draft, and sending one never clears a
draft you already had: the draft carries no relation, so restoring it into a
plain composer would quietly send an unrelated message.

## Swipe right to mark read

Drag a room row to the right in the chat list to mark it read; the asterisk
goes away. The row follows your finger up to 72 dp and springs back on release,
so the gesture shows itself rather than being a dead zone, and it commits at
48 dp — past the touch slop, short of the travel limit, so a wobble while
scrolling the list does nothing. It buzzes on commit, matching the row's
existing tap and long-press haptics.

Only unread rows respond: on a read row the gesture is not even installed, so
there is nothing to bump into. The read receipt is posted at the room's newest
event (`MarkRead`, which the thread already used on open), and the companion
serves that room as unread-0 immediately, so the quiet refresh that follows
drops the marker without waiting for a sync round.

| File | Change |
| --- | --- |
| `screens/ContextWindow.kt` | The REPLY row, on own and received messages alike. |
| `screens/ComposerScreen.kt` | `replyTarget`: the title, the draft rules, and `replyToEventId` on send. |
| `screens/ThreadScreen.kt` | `openComposer(replyTo = …)`, and keeping the room draft out of a reply's way. |
| `screens/ChatListScreen.kt` | The row's horizontal drag gesture, its travel/trigger geometry, and `markRoomRead`. |
