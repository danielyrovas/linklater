package org.yrovas.linklater.ui.state

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.domain.APIError
import org.yrovas.linklater.domain.BookmarkAPI
import org.yrovas.linklater.domain.BookmarkDataSource
import org.yrovas.linklater.domain.errorOrThrow
import org.yrovas.linklater.domain.ifOk
import org.yrovas.linklater.domain.isOk

const val TAG = "DEBUG/state"

@Inject
class HomeScreenState(
    private val api: BookmarkAPI,
    private val bookmarkSource: BookmarkDataSource,
) : ScreenState<HomeScreenState.Event, HomeScreenState.Effect>() {

    sealed interface Event : ScreenEvent {
        data object RefreshBookmarks : Event
    }

    sealed interface Effect : ScreenEffect {
        data class RefreshError(val error: APIError) : Effect
        data object RefreshOk : Effect
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _displayedBookmarks = MutableStateFlow(listOf<Bookmark>())
    val displayedBookmarks = _displayedBookmarks.asStateFlow()

    private val _bookmarkCount = MutableStateFlow(0)
    val bookmarkCount = _bookmarkCount.asStateFlow()

    private fun fetchLocalBookmarks() =
        viewModelScope.launch(Dispatchers.IO) {
            bookmarkSource.getBookmarks().collect { bookmarks ->
                _displayedBookmarks.update {
                    bookmarks.distinct().sortedByDescending { it.date_modified }
                }
                _bookmarkCount.emit(bookmarkSource.getBookmarkCount())
            }
        }

    private fun fetchRemoteBookmarks() =
        viewModelScope.launch(Dispatchers.IO) {
            // when authenticated refresh
            api.authProvided.transformWhile { emit(it); !it }.collect {
                if (it) {
                    refreshBookmarks()
                }
            }
        }

    private fun refreshBookmarks() {
        _isRefreshing.update { true }
        viewModelScope.launch(Dispatchers.IO) {
            val res = api.getBookmarks(page = 0)
            sendEffect(
                if (res.isOk) Effect.RefreshOk
                else Effect.RefreshError(res.errorOrThrow())
            )
            _isRefreshing.update { false }
            res.ifOk { bookmarkSource.insertBookmarks(it) }
        }
    }

    override fun handleEvent(event: Event) {
        when (event) {
            Event.RefreshBookmarks -> refreshBookmarks()
        }
    }

    init {
        fetchLocalBookmarks()
        fetchRemoteBookmarks()
    }
}
