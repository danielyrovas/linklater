package org.yrovas.linklater.data.models
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

const val PREF_STORE_NAME = "preferences"
object Prefs {
    val LINKDING_URL = stringPreferencesKey("linkding_url")
    val LINKDING_TOKEN = stringPreferencesKey("linkding_token")
    val BOOKMARK_DEFAULT_TAG_NAMES = stringPreferencesKey("bookmark_default_tag_names")
    val BOOKMARK_DEFAULT_UNREAD = booleanPreferencesKey("bookmark_default_unread")
    val BOOKMARK_DEFAULT_SHARED = booleanPreferencesKey("bookmark_default_shared")
    val BOOKMARK_DEFAULT_ARCHIVED = booleanPreferencesKey("bookmark_default_archived")

    // 1-6 in order: verbose, debug, info, warn, error, assert. Defaults to warn
    val NET_LOG_SEVERITY = intPreferencesKey("net_log_severity")
    val EVENT_LOG_SEVERITY = intPreferencesKey("event_log_severity")
    val LOG_SEVERITY = intPreferencesKey("log_severity")
    val LOG_ERRORS_TO_SENTRY = booleanPreferencesKey("log_errors_to_sentry")
}
