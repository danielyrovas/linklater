package org.yrovas.linklater.ui.screens.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.domain.APIError
import org.yrovas.linklater.domain.BookmarkAPI
import org.yrovas.linklater.domain.BookmarkDataSource
import org.yrovas.linklater.domain.errorOrThrow
import org.yrovas.linklater.domain.getOrThrow
import org.yrovas.linklater.domain.isErr
import org.yrovas.linklater.ui.screens.ScreenEffect
import org.yrovas.linklater.ui.screens.ScreenEvent
import org.yrovas.linklater.ui.screens.ScreenState

const val TAG = "DEBUG/state"
const val FIRST_POSSIBLE_DATE = "0000-01-01T00:00:00Z"

@Inject
class HomeState(
    private val api: BookmarkAPI,
    private val bookmarkSource: BookmarkDataSource,
) : ScreenState<HomeState.Event, HomeState.Effect>() {

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

    private fun fetchLocalBookmarks() = viewModelScope.launch(Dispatchers.IO) {
        bookmarkSource.getBookmarks().collect { bookmarks ->
            _displayedBookmarks.update {
                bookmarks.distinct().sortedByDescending { it.date_modified }
            }
            _bookmarkCount.emit(bookmarkSource.getBookmarkCount())
        }
    }

    private fun fetchRemoteBookmarks() = viewModelScope.launch(Dispatchers.IO) {
        // when authenticated refresh
        api.authProvided.transformWhile { emit(it); !it }.collect {
            if (it) {
                refreshAllBookmarks()
            }
        }
    }

    private fun refreshAllBookmarks() {
        _isRefreshing.update { true }
        viewModelScope.launch {
            var firstPage = true
            var last: Bookmark? = null
            api.getAllBookmarks().collect {
                if (it.isErr) {
                    Log.d(TAG, "fetchAllRemoteBookmarks: ERROR: ${it.errorOrThrow()}")
                    sendEffect(Effect.RefreshError(it.errorOrThrow()))
                    return@collect
                } else {
                    val bookmarks = it.getOrThrow()
                    Log.d(TAG, "fetchAllRemoteBookmarks: collected ${bookmarks.size} bookmarks")

                    if (bookmarks.isEmpty()) {
                        if (firstPage) {
                            val now = Clock.System.now().toString()
                            Log.d(TAG, "fetchAllRemoteBookmarks: DELETING ALL bookmarks")
                            bookmarkSource.deleteWithinRange(
                                startDate = FIRST_POSSIBLE_DATE, endDate = now
                            )
                        }
                        return@collect
                    }

                    if (bookmarks.first().date_added.isNullOrBlank()) {
                        Log.d(TAG, "fetchAllRemoteBookmarks: FIRST ADDED IS BLANK")
                        return@collect
                    }

                    if (bookmarks.last().date_added.isNullOrBlank()) {
                        Log.d(TAG, "fetchAllRemoteBookmarks: LAST ADDED IS BLANK")
                        return@collect
                    }

                    if (firstPage) {
                        firstPage = false
                        bookmarkSource.deleteWithinRange(
                            startDate = FIRST_POSSIBLE_DATE,
                            endDate = bookmarks.first().date_added!!,
                            exclude = listOf(bookmarks.first()) // exclude the first bookmark
                        )
                    }

                    last = bookmarks.last()
                    // delete from db where date_added older than newest on page,
                    // but younger than oldest on page - ie is in date range of page AND
                    // id not in the set of bookmarks returned from API.
                    bookmarkSource.upsertOrDeleteWithinRange(
                        bookmarks,
                        bookmarks.first().date_added!!,
                        last!!.date_added!!,
                    )
                }
            }

            last?.let {
                val now = Clock.System.now().toString()
                Log.d(TAG, "fetchAllRemoteBookmarks: deleting bookarks newer than $now")
                bookmarkSource.deleteWithinRange(
                    startDate = it.date_added!!, endDate = now, exclude = listOf(it)
                )
            }
            _isRefreshing.update { false }
            sendEffect(Effect.RefreshOk)
        }
    }

    override fun handleEvent(event: Event) {
        when (event) {
            Event.RefreshBookmarks -> refreshAllBookmarks()
        }
    }

    init {
        fetchLocalBookmarks()
        fetchRemoteBookmarks()
    }
}
