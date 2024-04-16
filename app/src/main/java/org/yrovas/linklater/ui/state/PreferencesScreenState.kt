package org.yrovas.linklater.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.data.LocalBookmark
import org.yrovas.linklater.data.local.PrefDataStore
import org.yrovas.linklater.data.local.Prefs
import org.yrovas.linklater.data.remote.BookmarkAPI
import org.yrovas.linklater.intoTags

//@Provides
//fun providePreferencesScreenState(appViewModel: () -> AppViewModelImpl): PreferencesScreenState =
//    PreferencesScreenState(appViewModel)

@Inject
class PreferencesScreenState(
    private val bookmarkAPI: BookmarkAPI,
    private val prefStore: PrefDataStore,
) : ViewModel() {
    private val _bookmarkEndpoint: MutableStateFlow<String> = MutableStateFlow("")
    var bookmarkEndpoint = _bookmarkEndpoint.asStateFlow()
    private val _bookmarkAPIToken: MutableStateFlow<String> = MutableStateFlow("")
    var bookmarkAPIToken = _bookmarkAPIToken.asStateFlow()

    private fun saveBookmarkConf(
        url: String? = null,
        token: String? = null,
    ) {
        if (url != null) _bookmarkEndpoint.update { url }
        if (token != null) _bookmarkAPIToken.update { token }

        viewModelScope.launch(Dispatchers.IO) {
            bookmarkAPI.authenticate(
                bookmarkEndpoint.value, bookmarkAPIToken.value
            )
            prefStore.setPref(Prefs.LINKDING_URL, bookmarkEndpoint.value)
            prefStore.setPref(Prefs.LINKDING_TOKEN, bookmarkAPIToken.value)
        }
    }

    fun saveBookmarkURL(url: String) {
        saveBookmarkConf(url = url)
    }

    fun saveBookmarkAPIToken(token: String) {
        saveBookmarkConf(token = token)
    }

    private val _tag_names: MutableStateFlow<String> = MutableStateFlow("")
    var tag_names = _tag_names.asStateFlow()
    private val _defaultBookmark: MutableStateFlow<LocalBookmark> =
        MutableStateFlow(LocalBookmark(""))
    var defaultBookmark = _defaultBookmark.asStateFlow()

    fun saveDefaultBookmark(
        tag_names: String? = null,
        unread: Boolean? = null,
        shared: Boolean? = null,
        is_archived: Boolean? = null,
    ) {
        updateDefaultBookmark(
            tag_names = tag_names,
            unread = unread,
            shared = shared,
            is_archived = is_archived,
        )
        viewModelScope.launch {
            prefStore.setPref(
                Prefs.BOOKMARK_DEFAULT_TAG_NAMES,
                defaultBookmark.value.tags.joinToString(separator = " ")
            )
            prefStore.setPref(
                Prefs.BOOKMARK_DEFAULT_UNREAD, defaultBookmark.value.unread
            )
            prefStore.setPref(
                Prefs.BOOKMARK_DEFAULT_SHARED, defaultBookmark.value.shared
            )
            prefStore.setPref(
                Prefs.BOOKMARK_DEFAULT_ARCHIVED, defaultBookmark.value.is_archived
            )
        }
    }

    private fun updateDefaultBookmark(
        tag_names: String? = null,
        unread: Boolean? = null,
        shared: Boolean? = null,
        is_archived: Boolean? = null,
    ) {
        this._tag_names.update { tag_names ?: "" }
        val tags = this.tag_names.value.intoTags()
        _defaultBookmark.update {
            it.withUpdates(is_archived = is_archived,
                unread = unread,
                shared = shared,
                tags = tags.ifEmpty { null })
        }
    }

    init {
        viewModelScope.launch {
            saveBookmarkAPIToken(prefStore.getPref(Prefs.LINKDING_TOKEN, ""))
            saveBookmarkURL(prefStore.getPref(Prefs.LINKDING_URL, ""))
            updateDefaultBookmark(
                tag_names = prefStore.getPref(
                    Prefs.BOOKMARK_DEFAULT_TAG_NAMES, ""
                ),
                unread = prefStore.getPref(Prefs.BOOKMARK_DEFAULT_UNREAD, false),
                shared = prefStore.getPref(Prefs.BOOKMARK_DEFAULT_SHARED, false),
                is_archived = prefStore.getPref(
                    Prefs.BOOKMARK_DEFAULT_ARCHIVED, false
                ),
            )
        }
    }
}
