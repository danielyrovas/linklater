package org.yrovas.linklater.data.local

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface PrefStore {
    suspend fun <T> getPrefs(key: Preferences.Key<T>, default: T): Flow<T>
    suspend fun <T> getPref(key: Preferences.Key<T>, default: T): T

    suspend fun <T> setPref(key: Preferences.Key<T>, value: T)
    suspend fun <T> removePref(key: Preferences.Key<T>)
    suspend fun <T> clearPrefs()
}
