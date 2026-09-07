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

### Showing the quote

A reply row draws the message it answers as a quiet one-line quote above the
reply — `> the message being answered`, Superfine and ellipsised, the same
grammar as the "forwarded" and "edited" tags.

The quote is Matrix's own **rich-reply fallback**: bridged clients put the
answered message at the top of the reply body as `> <@ada:server> …`, a blank
line, then the reply. Upstream's companion threw that away
(`stripReplyQuote`); this fork keeps it on thread rows — previews and
notifications still strip it, so the room list shows words, not quotes — and
the tool splits it with `splitReplyQuote` in `Format.kt`.

Because the fallback rides in the body, **every consumer of a row body splits
it**: the reply half is what a retry re-sends, what optimistic echoes are
matched against, what an edit prefills, and what the unsend confirmation
shows. Add a new consumer and it must do the same, or the quoted lines leak
into a real message.

**The limit:** your own replies show no quote. Trixnity writes the
`m.in_reply_to` relation without a fallback body, so there is nothing to split
— the relation is there (the recipient's client quotes correctly), but this
screen has no quoted text to draw. Fixing that properly needs reply fields on
`GetMessages.Message`, which lives in the SDK: a fork of `fenleon/light-sdk`,
deliberately not taken here.

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
