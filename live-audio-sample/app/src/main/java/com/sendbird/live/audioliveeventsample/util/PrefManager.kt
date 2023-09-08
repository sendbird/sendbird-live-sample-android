package com.sendbird.live.audioliveeventsample.util

import android.content.Context

private const val PREFERENCE_AUDIO_ONLY_LIVE_EVENT_SAMPLE = "sendbird_audio_only_live_event_sample_preference"
private const val PREF_KEY_APPLICATION_ID = "pref_key_application_id"
private const val PREF_KEY_USER_ID = "pref_key_user_id"
private const val PREF_KEY_ACCESS_TOKEN = "pref_key_access_token"

class PrefManager(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCE_AUDIO_ONLY_LIVE_EVENT_SAMPLE, Context.MODE_PRIVATE)

    var appId: String?
        get() = preferences.getString(PREF_KEY_APPLICATION_ID, null)
        set(value) {
            if (value == null) {
                preferences.edit().remove(PREF_KEY_APPLICATION_ID).apply()
            } else {
                preferences.edit().putString(PREF_KEY_APPLICATION_ID, value).apply()
            }
        }

    var userId: String?
        get() = preferences.getString(PREF_KEY_USER_ID, null)
        set(value) {
            if (value == null) {
                preferences.edit().remove(PREF_KEY_USER_ID).apply()
            } else {
                preferences.edit().putString(PREF_KEY_USER_ID, value).apply()
            }
        }

    var accessToken: String?
        get() = preferences.getString(PREF_KEY_ACCESS_TOKEN, null)
        set(value) {
            if (value == null) {
                preferences.edit().remove(PREF_KEY_ACCESS_TOKEN).apply()
            } else {
                preferences.edit().putString(PREF_KEY_ACCESS_TOKEN, value).apply()
            }
        }

    fun removeAll() {
        appId = null
        userId = null
        accessToken = null
    }
}
