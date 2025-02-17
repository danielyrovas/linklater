package org.yrovas.linklater.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Severity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.io.IOException
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.InitLog
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

object Prefs {
    val LINKDING_URL = stringPreferencesKey("linkding_url")
    val LINKDING_TOKEN = stringPreferencesKey("linkding_token")
    val BOOKMARK_DEFAULT_TAG_NAMES = stringPreferencesKey("bookmark_default_tag_names")
    val BOOKMARK_DEFAULT_UNREAD = booleanPreferencesKey("bookmark_default_unread")
    val BOOKMARK_DEFAULT_SHARED = booleanPreferencesKey("bookmark_default_shared")
    val BOOKMARK_DEFAULT_ARCHIVED = booleanPreferencesKey("bookmark_default_archived")

    // 1-6 in order: verbose, debug, info, warn, error, assert. Defaults to warn
    val NET_LOG_SEVERITY = intPreferencesKey("net_log_severity")
    val INIT_LOG_SEVERITY = intPreferencesKey("init_log_severity")
    val EVENT_LOG_SEVERITY = intPreferencesKey("event_log_severity")
    val LOG_SEVERITY = intPreferencesKey("log_severity")
}

fun Int.asSeverity(): Severity {
    return when (this) {
        1 -> Severity.Verbose
        2 -> Severity.Debug
        3 -> Severity.Info
        4 -> Severity.Warn
        5 -> Severity.Error
        6 -> Severity.Assert
        else -> Severity.Warn // should silently handle? or throw Throwable("value is not a severity value: $this")
    }
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class PrefStore(private val store: DataStore<Preferences>) : PrefDataStore {
    init {
        InitLog.v { "Creating Preferences DataStore" }
    }
    override suspend fun <T> getPrefs(
        key: Preferences.Key<T>,
        default: T,
    ): Flow<T> = store.data.catch {
        if (it is IOException) emit(emptyPreferences()) else throw it
    }.map { it[key] ?: default }

    override suspend fun <T> getPref(key: Preferences.Key<T>, default: T): T =
        store.data.first()[key] ?: default

    override suspend fun <T> setPref(key: Preferences.Key<T>, value: T) {
        store.edit { pref -> pref[key] = value }
    }

    override suspend fun <T> removePref(key: Preferences.Key<T>) {
        store.edit { pref -> pref.remove(key) }
    }

    override suspend fun <T> clearPrefs() {
        store.edit { pref -> pref.clear() }
    }
}
