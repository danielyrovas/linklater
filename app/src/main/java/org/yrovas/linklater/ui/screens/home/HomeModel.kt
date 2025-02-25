package org.yrovas.linklater.ui.screens.home

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.unwrap
import com.github.michaelbull.result.unwrapError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.Log
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.data.local.BookmarkDataSource
import org.yrovas.linklater.data.models.APIError
import org.yrovas.linklater.data.remote.BookmarkAPI
import org.yrovas.linklater.data.showTitleOrElse
import org.yrovas.linklater.ui.screens.ScreenEffect
import org.yrovas.linklater.ui.screens.ScreenEvent
import org.yrovas.linklater.ui.screens.ScreenModel

const val FIRST_POSSIBLE_DATE = "0000-01-01T00:00:00Z"

@Inject
class HomeModel(
    private val api: BookmarkAPI,
    private val bookmarkSource: BookmarkDataSource,
) : ScreenModel<HomeModel.Event, HomeModel.Effect>() {

    sealed interface Event : ScreenEvent {
        data object RefreshBookmarks : Event
        data object SearchBarClose : Event
//        data object UpdateQuery: Event
    }

    sealed interface Effect : ScreenEffect {
        data class RefreshError(val error: APIError) : Effect
        data object RefreshOk : Effect
        data object FilterChanged : Effect
    }

    init {
        fetchLocalBookmarks()
        fetchRemoteBookmarks()
    }

    override fun handleEvent(event: Event) {
        when (event) {
            Event.RefreshBookmarks -> refreshAllBookmarks()
            Event.SearchBarClose -> clearSearch()
//            is Event.UpdateQuery -> updateQuery()
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _allBookmarks = MutableStateFlow(listOf<Bookmark>())
    private val allBookmarks = _allBookmarks.asStateFlow()

    private val _bookmarkCount = MutableStateFlow(0)
    val bookmarkCount = _bookmarkCount.asStateFlow()

    val bookmarkQueryState = TextFieldState()

//    private val _displayedBookmarks = MutableStateFlow(listOf<Bookmark>())
//    val displayedBookmarks = _displayedBookmarks.asStateFlow()

    val filteredBookmarks: StateFlow<List<Bookmark>> =
        snapshotFlow { bookmarkQueryState.text }.debounce(500).mapLatest { query ->
            val b = allBookmarks.value.filter { bookmarkMatchesQuery(it, query.toString()) }
            sendEffect(Effect.FilterChanged)
            b
        }.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(), initialValue = allBookmarks.value
        )

    val filteredBookmarkCount: StateFlow<Int> = snapshotFlow { filteredBookmarks.value }.mapLatest {
        it.size
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), initialValue = allBookmarks.value.size
    )

    private val _searchBarActive = MutableStateFlow(false)
    val searchBarActive = _searchBarActive.asStateFlow()

    private fun bookmarkMatchesQuery(bookmark: Bookmark, query: String): Boolean {
        val stringWithoutTags = stringWithoutTags(query)
        val stringWithoutTagSymbols = stringWithoutTagSymbols(query)
        val partialTag = partialTagFromQuery(query)
        val tags = tagsFromQuery(query)
        return if (stringWithoutTags.isBlank()) queryMatchesTags(bookmark, tags, partialTag)
        else queryMatchesTags(bookmark, tags, partialTag) && (queryMatchesString(
            bookmark, query
        ) || queryMatchesString(bookmark, stringWithoutTags) || queryMatchesString(
            bookmark, stringWithoutTagSymbols
        ))
    }

    private fun queryMatchesString(bookmark: Bookmark, string: String): Boolean {
        return bookmark.url.contains(string, ignoreCase = true) || bookmark.title?.contains(
            string, ignoreCase = true
        ) == true || bookmark.website_title?.contains(
            string, ignoreCase = true
        ) == true || bookmark.description?.contains(
            string, ignoreCase = true
        ) == true || bookmark.website_description?.contains(
            string, ignoreCase = true
        ) == true || bookmark.notes?.contains(string, ignoreCase = true) == true
    }

    private fun queryMatchesTags(
        bookmark: Bookmark, tags: List<String>, partialTag: String
    ): Boolean {
        return tags.all { queryTag ->
            bookmark.tags.any {
                it.equals(queryTag, ignoreCase = true) || (queryTag == partialTag && it.startsWith(
                    partialTag, ignoreCase = true
                ))
            }
        }
    }

    private fun tagsFromQuery(query: String): List<String> {
        return query.trim().split("\\s+".toRegex()).filter { it.startsWith('#') }
            .map { it.split('#').last() }.filter { it.isNotBlank() }
    }

    private fun partialTagFromQuery(query: String): String {
        val lastString = query.split("\\s+".toRegex()).last()
        return if (lastString.startsWith('#')) lastString.split('#').last()
        else ""
    }

    private fun stringWithoutTags(query: String): String {
        return query.trim().split("\\s+".toRegex()).filter { !it.startsWith('#') }.joinToString(" ")
    }

    private fun stringWithoutTagSymbols(query: String): String {
        return query.trim().split("\\s+".toRegex()).joinToString(" ") {
            if (it.startsWith('#')) {
                it.split('#').last()
            } else {
                it
            }
        }
    }

    private fun clearSearch() {
        _searchBarActive.update { false }
    }

    private fun fetchLocalBookmarks() = viewModelScope.launch(Dispatchers.IO) {
        bookmarkSource.getBookmarks().collect { bookmarks ->
            _allBookmarks.update {
                bookmarks.distinct().sortedByDescending { it.date_modified }
            }
            _bookmarkCount.emit(bookmarkSource.getBookmarkCount())
        }
    }

    private fun fetchRemoteBookmarks() = viewModelScope.launch(Dispatchers.IO) {
        // wait for authentication, then refresh
        api.authProvided.transformWhile { emit(it); !it }.collect {
            if (it) {
                refreshAllBookmarks()
            }
        }
    }

    // TODO: prompt to delete all when API returns empty result set, handle offline case
    // and provide function to save bookmarks when offline & sync once online.
    private fun refreshAllBookmarks() {
        _isRefreshing.update { true }
        viewModelScope.launch {
            var last: Bookmark? = null
            var page = 0
            api.getAllBookmarks().collect { res ->
                if (res.isErr) {
                    Log.w { "Error refreshing bookmarks on page $page: ${res.unwrapError()}" }
                    sendEffect(Effect.RefreshError(res.unwrapError()))
                    page += 1
                    return@collect
                }
                val bookmarks = res.unwrap()

                if (bookmarks.isEmpty()) {
                    if (page == 0) {
                        val now = Clock.System.now().toString()
                        Log.d { "LinkDing server did not send any bookmarks. Deleting all local bookmarks." }
                        bookmarkSource.deleteWithinRange(
                            startDate = FIRST_POSSIBLE_DATE, endDate = now
                        )
                    }
                    return@collect
                }

                val firstBookmark = bookmarks.first()

                if (firstBookmark.date_added.isNullOrBlank()) {
                    Log.w {
                        "First bookmark's added date is blank, you should delete and recreate this bookmark:\n" +
                            "${firstBookmark.id} :: ${firstBookmark.showTitleOrElse(firstBookmark.url)}"
                    }
                    return@collect
                }

                val lastBookmark = bookmarks.last()
                if (lastBookmark.date_added.isNullOrBlank()) {
                    Log.w {
                        "Final bookmark's added date is blank, you should delete and recreate this bookmark:\n" +
                            "${lastBookmark.id} :: ${lastBookmark.showTitleOrElse(lastBookmark.url)}"
                    }
                    return@collect
                }

                if (page == 0) {
                    // TODO: Is this logic borked? INVESTIGATE
                    bookmarkSource.deleteWithinRange(
                        startDate = FIRST_POSSIBLE_DATE,
                        endDate = firstBookmark.date_added,
                        exclude = listOf(firstBookmark) // exclude the first bookmark
                    )
                }

                page += 1
                last = lastBookmark
                // delete from db where date_added older than newest on page,
                // but younger than oldest on page - ie is in date range of page AND
                // id not in the set of bookmarks returned from API.
                bookmarkSource.upsertOrDeleteWithinRange(
                    bookmarks,
                    bookmarks.first().date_added!!,
                    last!!.date_added!!,
                )
            }

            last?.let {
                val now = Clock.System.now().toString()
                Log.d {
                    "Deleting bookmarks newer than ${it.date_added}, the most recent bookmark returned by LinkDing: ${it.showTitleOrElse(it.url)}"
                }
                bookmarkSource.deleteWithinRange(
                    startDate = it.date_added!!, endDate = now, exclude = listOf(it)
                )
            }
            _isRefreshing.update { false }
            sendEffect(Effect.RefreshOk)
        }
    }
}
