package com.lightphone.chats

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.thelightphone.sdk.SealedLightContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

/**
 * Tool-local settings, persisted in the SDK's DataStore
 * ([SealedLightContext.dataStore]). The "seen" marker + data-saver
 * preferences live here: Settings toggles them, the thread reads the flows.
 */
object ChatSettings {

    private val KEY_SHOW_READ_STATUS = booleanPreferencesKey("chats.show_read_status")
    private val KEY_DOWNLOAD_OVER_MOBILE = booleanPreferencesKey("chats.download_over_mobile")
    private val KEY_DEVICE_KEYBOARD = booleanPreferencesKey("chats.device_keyboard")

    /** Whether the thread shows "seen" under outgoing messages. Default on. */
    val showReadStatus = MutableStateFlow(true)

    /** Data Saver Mode: true = media downloads restricted to Wi-Fi. The Settings
     *  toggle shows the inverse of this flag (checked = saver ON). Defaults to
     *  mobile-allowed (feedback 2026-08-19: "Data Saver Mode … default OFF"). */
    val downloadOverMobile = MutableStateFlow(true)

    /** Whether text entry uses the phone's own keyboard (the system IME, so a
     *  third-party keyboard types into Chats) instead of the embedded LightOS
     *  LP3 keyboard. This fork's reason for existing, so it defaults ON; the
     *  Settings toggle switches back to the LP3 keys — the escape hatch for a
     *  phone with no IME enabled, where the system keyboard never appears. */
    val deviceKeyboard = MutableStateFlow(true)

    private var loaded = false

    /** Loads the persisted values once (idempotent); call from any screen's scope. */
    suspend fun load(lightContext: SealedLightContext) {
        if (loaded) return
        loaded = true
        runCatching {
            val prefs = lightContext.dataStore.data.first()
            showReadStatus.value = prefs[KEY_SHOW_READ_STATUS] ?: true
            downloadOverMobile.value = prefs[KEY_DOWNLOAD_OVER_MOBILE] ?: true
            deviceKeyboard.value = prefs[KEY_DEVICE_KEYBOARD] ?: true
        }
    }

    /** Persists and publishes the show-read-status toggle value. */
    suspend fun setShowReadStatus(lightContext: SealedLightContext, value: Boolean) {
        showReadStatus.value = value
        runCatching {
            lightContext.dataStore.edit { it[KEY_SHOW_READ_STATUS] = value }
        }
    }

    /** Persists and publishes the mobile-data-downloads toggle value. */
    suspend fun setDownloadOverMobile(lightContext: SealedLightContext, value: Boolean) {
        downloadOverMobile.value = value
        runCatching {
            lightContext.dataStore.edit { it[KEY_DOWNLOAD_OVER_MOBILE] = value }
        }
    }

    /** Persists and publishes the device-keyboard toggle value. */
    suspend fun setDeviceKeyboard(lightContext: SealedLightContext, value: Boolean) {
        deviceKeyboard.value = value
        runCatching {
            lightContext.dataStore.edit { it[KEY_DEVICE_KEYBOARD] = value }
        }
    }
}
