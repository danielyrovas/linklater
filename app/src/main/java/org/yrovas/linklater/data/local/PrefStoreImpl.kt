package org.yrovas.linklater.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.io.IOException
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.AppScope
import org.yrovas.linklater.Log

@Inject
@AppScope
class PrefStoreImpl(private val store: DataStore<Preferences>) : PrefStore {
    init {
        Log.v { "Creating Preferences DataStore" }
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

