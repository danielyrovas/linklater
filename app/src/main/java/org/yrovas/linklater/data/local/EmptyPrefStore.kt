package org.yrovas.linklater.data.local

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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
