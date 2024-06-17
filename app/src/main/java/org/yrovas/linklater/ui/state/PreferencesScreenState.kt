package org.yrovas.linklater.ui.state

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.ApplicationScope
import org.yrovas.linklater.data.LocalBookmark
import org.yrovas.linklater.data.local.PrefDataStore
import org.yrovas.linklater.data.local.Prefs
import org.yrovas.linklater.domain.BookmarkAPI
import org.yrovas.linklater.domain.BookmarkDataSource
import org.yrovas.linklater.domain.Res
import org.yrovas.linklater.intoTags
import org.yrovas.linklater.ui.state.PreferencesScreenState.Effect
import org.yrovas.linklater.ui.state.PreferencesScreenState.Event

@Inject
class PreferencesScreenState(
    private val bookmarkAPI: BookmarkAPI,
    private val prefStore: PrefDataStore,
    private val appScope: ApplicationScope,
    private val bookmarkSource: BookmarkDataSource,
    private val api: BookmarkAPI,
) : ScreenState<Event, Effect>() {

    sealed interface Event : ScreenEvent {
        data object FetchAllBookmarks : Event
        data class SaveEndpoint(val url: String) : Event
        data class SaveToken(val token: String) : Event
        data class SaveDefaults(
            val tag_names: String? = null,
            val unread: Boolean? = null,
            val shared: Boolean? = null,
        ) : Event
    }

    sealed interface Effect : ScreenEffect {}

    private val _bookmarkEndpoint = MutableStateFlow("")
    var bookmarkEndpoint = _bookmarkEndpoint.asStateFlow()
    // TODO backing fields
//    val bookmarkEndpoint: StateFlow<String>
//        field = MutableStateFlow("")

    private val _bookmarkAPIToken = MutableStateFlow("")
    var bookmarkAPIToken = _bookmarkAPIToken.asStateFlow()

    private val _tag_names: MutableStateFlow<String> = MutableStateFlow("")
    var tag_names = _tag_names.asStateFlow()

    private val _defaultBookmark = MutableStateFlow(LocalBookmark(""))
    var defaultBookmark = _defaultBookmark.asStateFlow()

    private fun saveBookmarkConf(url: String? = null, token: String? = null) {
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

    private fun saveDefaultBookmark(
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
            it.withUpdates(
                is_archived = is_archived,
                unread = unread,
                shared = shared,
                tags = tags
            )
        }
    }

    private fun fetchAllRemoteBookmarks() {
        // guard against going back to home by launching from application scope
        appScope.launch(Dispatchers.IO) {
            var res = api.getBookmarks(0)
            var page = 0
            while (true) when (res) {
                is Res.Err -> {
                    Log.d(
                        TAG,
                        "fetchAllRemoteBookmarks: stopping due to ${res.error}"
                    )
                    break
                }

                is Res.Ok -> {
                    if (res.data.isEmpty()) {
                        Log.d(
                            TAG,
                            "fetchAllRemoteBookmarks: stopping due to empty result set"
                        )
                        break
                    }
                    Log.d(
                        TAG,
                        "fetchAllRemoteBookmarks: FETCHED ${res.data} from page $page"
                    )
                    bookmarkSource.insertBookmarks(res.data)
                    res = api.getBookmarks(page++)
                }
            }
            appScope.showSnackbar("Refresh Complete")
        }
    }


    init {
        viewModelScope.launch {
            _bookmarkAPIToken.update {
                prefStore.getPref(Prefs.LINKDING_TOKEN, "")
            }
            _bookmarkEndpoint.update {
                prefStore.getPref(Prefs.LINKDING_URL, "")
            }
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

    override fun handleEvent(event: Event) {
        when (event) {
            is Event.FetchAllBookmarks -> fetchAllRemoteBookmarks()
            is Event.SaveDefaults -> saveDefaultBookmark(
                tag_names = event.tag_names,
                unread = event.unread,
                shared = event.shared,
            )

            is Event.SaveEndpoint -> saveBookmarkConf(url = event.url)
            is Event.SaveToken -> saveBookmarkConf(token = event.token)
        }
    }
}
