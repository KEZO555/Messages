package com.lightphone.chats.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActionHandler
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.lightphone.chats.ChatSettings
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightBottomBar
import com.thelightphone.sdk.ui.LightIconConfiguration
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.scaledForScreenHeight

/**
 * Text entry with the phone's own keyboard.
 *
 * Upstream every editor is [com.thelightphone.sdk.ui.LightTextInputEditor],
 * which draws the closed light-keyboard component inside the app window: the
 * LightOS keys are the only keys, whatever IME the user installed. This fork
 * adds a second editor that is a plain Compose `BasicTextField` — focusing it
 * opens the system IME, so a third-party keyboard (Gboard, HeliBoard,
 * Unexpected Keyboard, …) types into Chats like it does into any other
 * Android app.
 *
 * [ChatsTextInputEditor] is the switch every screen calls: it renders this
 * editor while Settings → Device Keyboard is on (the fork's default) and the
 * upstream LP3 editor — passed verbatim as [lp3Editor] — when it's off. The
 * toggle is the escape hatch: a phone with no enabled IME (a stock LightOS
 * install has none) shows no keyboard at all here, and the LP3 keys are one
 * switch away.
 *
 * Both editors drive the same [TextFieldState], so the screens' formatting and
 * draft-saving effects are untouched by the choice.
 */
@Composable
internal fun ChatsTextInputEditor(
    title: String,
    state: TextFieldState,
    onSubmit: (CharSequence) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    submitLabel: String = "SUBMIT",
    submitIcon: LightIconConfiguration? = null,
    singleLine: Boolean = true,
    submitOnReturn: Boolean = true,
    bottomAligned: Boolean = false,
    submitInTopBar: Boolean = false,
    topBarSubmitIcon: LightIconConfiguration? = null,
    initialCaps: Boolean = false,
    centered: Boolean = false,
    inputTextStyle: TextStyle? = null,
    imeAction: ImeAction = ImeAction.Done,
    lp3Editor: @Composable () -> Unit,
) {
    val deviceKeyboard by ChatSettings.deviceKeyboard.collectAsState()
    if (!deviceKeyboard) {
        lp3Editor()
        return
    }
    DeviceKeyboardTextInputEditor(
        title = title,
        state = state,
        onSubmit = onSubmit,
        onBack = onBack,
        modifier = modifier,
        submitLabel = submitLabel,
        submitIcon = submitIcon,
        singleLine = singleLine,
        submitOnReturn = submitOnReturn,
        bottomAligned = bottomAligned,
        submitInTopBar = submitInTopBar,
        topBarSubmitIcon = topBarSubmitIcon,
        initialCaps = initialCaps,
        centered = centered,
        inputTextStyle = inputTextStyle,
        imeAction = imeAction,
    )
}

/**
 * The system-IME editor. Same chrome as the LP3 one — back arrow and title in
 * the top bar, the submit action in the bottom bar (or the top-right corner
 * when [submitInTopBar]) — with the keyboard half of the screen left to the
 * IME, which the field's focus request opens and the window insets make room
 * for.
 */
@Composable
private fun DeviceKeyboardTextInputEditor(
    title: String,
    state: TextFieldState,
    onSubmit: (CharSequence) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier,
    submitLabel: String,
    submitIcon: LightIconConfiguration?,
    singleLine: Boolean,
    submitOnReturn: Boolean,
    bottomAligned: Boolean,
    submitInTopBar: Boolean,
    topBarSubmitIcon: LightIconConfiguration?,
    initialCaps: Boolean,
    centered: Boolean,
    inputTextStyle: TextStyle?,
    imeAction: ImeAction,
) {
    // No window plumbing here: LightActivity calls
    // WindowCompat.setDecorFitsSystemWindows(window, false) at start-up, so the
    // IME insets reach Compose and [Modifier.imePadding] below keeps the bottom
    // bar (and the composer's clear-draft X) above the keys. Tool code cannot
    // reach the Activity anyway — the SDK's build plugin rejects LocalContext
    // and android.app imports outright.

    // Opening an editor is always a request to type: take focus on the first
    // frame so the IME comes up without a tap on the field.
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    val submit = { onSubmit(state.text) }
    val textStyle = inputTextStyle ?: LightThemeTokens.typography.copy
        .copy(color = LightThemeTokens.colors.content)
        .scaledForScreenHeight()

    Column(modifier = Modifier.fillMaxSize().then(modifier).imePadding()) {
        LightTopBar(
            leftButton = LightBarButton.LightIcon(
                icon = LightIcons.BACK,
                onClick = onBack,
                contentDescription = "Back",
            ),
            center = LightTopBarCenter.Text(title),
            rightButton = if (submitInTopBar && topBarSubmitIcon != null) {
                LightBarButton.LightIcon(
                    icon = topBarSubmitIcon,
                    onClick = { submit() },
                    contentDescription = submitLabel,
                )
            } else {
                null
            },
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 2f.gridUnitsAsDp(), vertical = 1f.gridUnitsAsDp()),
            // Notes-style entry grows upward from the keyboard when the screen
            // asks for it (the composer, the recovery key); login fields centre
            // between the bars; everything else sits under the top bar.
            contentAlignment = when {
                bottomAligned -> Alignment.BottomStart
                centered -> Alignment.Center
                else -> Alignment.TopStart
            },
        ) {
            BasicTextField(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = textStyle,
                lineLimits = if (singleLine) {
                    TextFieldLineLimits.SingleLine
                } else {
                    TextFieldLineLimits.MultiLine()
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = if (initialCaps) {
                        KeyboardCapitalization.Sentences
                    } else {
                        KeyboardCapitalization.None
                    },
                    // Default = the IME shows its plain return key, which
                    // inserts a newline (the composer: messages span lines and
                    // SEND lives in the top bar).
                    imeAction = if (submitOnReturn) imeAction else ImeAction.Default,
                ),
                onKeyboardAction = if (submitOnReturn) {
                    KeyboardActionHandler { submit() }
                } else {
                    null
                },
                cursorBrush = SolidColor(LightThemeTokens.colors.content),
            )
        }
        LightBottomBar(
            modifier = Modifier.navigationBarsPadding(),
            items = if (submitInTopBar) {
                // The submit moved to the top bar; the row stays empty so the
                // composer's clear-draft X keeps its slot (three null slots =
                // the SDK's spacer bar).
                listOf(null, null, null)
            } else {
                listOf(
                    submitIcon?.let {
                        LightBarButton.LightIcon(
                            icon = it,
                            onClick = { submit() },
                            contentDescription = submitLabel,
                        )
                    } ?: LightBarButton.Text(
                        // Bar text buttons are uppercase (DESIGN.md).
                        text = submitLabel.uppercase(),
                        onClick = { submit() },
                    ),
                )
            },
        )
    }
}
