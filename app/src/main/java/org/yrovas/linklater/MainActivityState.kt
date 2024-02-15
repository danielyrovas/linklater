package org.yrovas.linklater

import android.content.Context
import android.widget.Toast
import androidx.annotation.Keep
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.yrovas.linklater.data.Bookmark

@Keep
object Prefs {
    val LINKDING_URL = stringPreferencesKey("linkding_url")
    val LINKDING_TOKEN = stringPreferencesKey("linkding_token")
}

@Keep
class MainActivityState : ViewModel() {
    private var bookmarkAPI: BookmarkAPI = EmptyBookmarkAPI()

    private val _isRefreshing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var isRefreshing = _isRefreshing.asStateFlow()

    private val _bookmarkURL: MutableStateFlow<String> = MutableStateFlow("")
    var bookmarkURL = _bookmarkURL.asStateFlow()
    suspend fun saveBookmarkURL(url: String, context: Context) {
        _bookmarkURL.value = url
        context.dataStore.edit { preferences ->
            preferences[Prefs.LINKDING_URL] = url
        }
        bookmarkAPI = LinkDingAPI(bookmarkURL.value, bookmarkAPIToken.value)
    }

    private val _bookmarkAPIToken: MutableStateFlow<String> = MutableStateFlow("")
    var bookmarkAPIToken = _bookmarkAPIToken.asStateFlow()
    suspend fun saveBookmarkAPIToken(token: String, context: Context) {
        _bookmarkAPIToken.value = token
        context.dataStore.edit { preferences ->
            preferences[Prefs.LINKDING_TOKEN] = token
        }
        bookmarkAPI = LinkDingAPI(bookmarkURL.value, bookmarkAPIToken.value)
    }

    fun checkBookmarkAPIToken(token: String) = token.length > 10

    private suspend fun loadPrefs(context: Context) {
        context.dataStore.data.first { preferences ->
            _bookmarkURL.update { preferences[Prefs.LINKDING_URL].orEmpty() }
            _bookmarkAPIToken.update { preferences[Prefs.LINKDING_TOKEN].orEmpty() }
            true
        }
        bookmarkAPI = LinkDingAPI(bookmarkURL.value, bookmarkAPIToken.value)
    }

    // Expose screen UI state
    private val _displayedBookmarks = MutableStateFlow(listOf<Bookmark>())
    val displayedBookmarks: StateFlow<List<Bookmark>> =
        _displayedBookmarks.asStateFlow()

    private val _displayedTags = MutableStateFlow(listOf<String>())
    val displayedTags: StateFlow<List<String>> = _displayedTags.asStateFlow()

    private suspend fun getBookmarks(page: Int = 0) = _displayedBookmarks.update {
        bookmarkAPI.getBookmarks(page = page, query = null)
            .getOrDefault(emptyList())
    }

    private suspend fun cacheBookmarks(context: Context) {
        bookmarkAPI.cacheBookmarks(context, displayedBookmarks.value)
    }

    private suspend fun cacheTags(context: Context) {
        bookmarkAPI.cacheTags(context, displayedTags.value)
    }

    private suspend fun loadLocalBookmarks(context: Context) {
        val bookmarks = bookmarkAPI.getCachedBookmarks(context)
        if (displayedBookmarks.value.isEmpty()) {
            _displayedBookmarks.emit(bookmarks)
        }
    }

    private suspend fun loadLocalTags(context: Context) {
        val tags = bookmarkAPI.getCachedTags(context)
        if (displayedTags.value.isEmpty()) {
            _displayedTags.emit(tags)
        }
    }

    private suspend fun tagsFromCachedBookmarks() {
        val tags = displayedBookmarks.value.flatMap { it.tags }.distinct()
        _displayedTags.update {
            (it + tags).distinct()
        }
    }

    suspend fun setup(context: Context) {
        viewModelScope.launch {
            loadPrefs(context)
            loadLocalBookmarks(context) // get cached page
            loadLocalTags(context) // get cached page
        }.join()
        refresh(context)
    }

    private suspend fun doRefresh(context: Context) {
        viewModelScope.launch {
            _isRefreshing.emit(true)
            getBookmarks()
            cacheBookmarks(context)
            _isRefreshing.emit(false)
            tagsFromCachedBookmarks()
            cacheTags(context)
        }.join()
    }

    private fun toast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    fun refresh(context: Context) {
        viewModelScope.launch { doRefresh(context) }
    }
}
