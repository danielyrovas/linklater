package org.yrovas.linklater.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.AppScope

object Prefs {
    val LINKDING_URL = stringPreferencesKey("linkding_url")
    val LINKDING_TOKEN = stringPreferencesKey("linkding_token")
}

@AppScope
@Inject
class PreferenceStore(private val store: DataStore<Preferences>) : PrefDataStore {
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

class EmptyPrefStore : PrefDataStore {
    override suspend fun <T> getPrefs(
        key: Preferences.Key<T>,
        default: T,
    ): Flow<T> = flow {}

    override suspend fun <T> getPref(key: Preferences.Key<T>, default: T): T =
        default

    override suspend fun <T> setPref(key: Preferences.Key<T>, value: T) {}
    override suspend fun <T> removePref(key: Preferences.Key<T>) {}
    override suspend fun <T> clearPrefs() {}
}
