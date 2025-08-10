package org.yrovas.linklater.ui.usecases

import com.github.michaelbull.result.unwrap
import com.github.michaelbull.result.unwrapError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.AppScope
import org.yrovas.linklater.Log
import org.yrovas.linklater.data.local.BookmarkDataSource
import org.yrovas.linklater.data.models.APIError
import org.yrovas.linklater.data.models.Bookmark
import org.yrovas.linklater.data.models.showTitleOrElse
import org.yrovas.linklater.data.remote.BookmarkAPI
import org.yrovas.linklater.ui.screens.home.FIRST_POSSIBLE_DATE
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@AppScope
@Inject
class BookmarkSyncUseCase(
    private val api: BookmarkAPI, private val bookmarkSource: BookmarkDataSource
) {

    // TODO: prompt to delete all when API returns empty result set, handle offline case
    // and provide function to save bookmarks when offline & sync once online.
    @OptIn(ExperimentalTime::class)
    suspend fun refreshAllBookmarksFromAPI(onError: (APIError) -> Unit) {
        var last: Bookmark? = null
        var page = 0
        api.getAllBookmarks().collect { res ->
            if (res.isErr) {
                Log.w { "Error refreshing bookmarks on page $page: ${res.unwrapError()}" }
                onError(res.unwrapError())
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
                    "First bookmark's added date is blank, you should delete and recreate this bookmark:\n" + "${firstBookmark.id} :: ${
                        firstBookmark.showTitleOrElse(
                            firstBookmark.url
                        )
                    }"
                }
                return@collect
            }

            val lastBookmark = bookmarks.last()
            if (lastBookmark.date_added.isNullOrBlank()) {
                Log.w {
                    "Final bookmark's added date is blank, you should delete and recreate this bookmark:\n" + "${lastBookmark.id} :: ${
                        lastBookmark.showTitleOrElse(
                            lastBookmark.url
                        )
                    }"
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
                last.date_added!!,
            )
        }

        last?.let {
            val now = Clock.System.now().toString()
            Log.d {
                "Deleting bookmarks newer than ${it.date_added}, the most recent bookmark returned by LinkDing: ${
                    it.showTitleOrElse(
                        it.url
                    )
                }"
            }
            bookmarkSource.deleteWithinRange(
                startDate = it.date_added!!, endDate = now, exclude = listOf(it)
            )
        }
    }

    private val _allBookmarks = MutableStateFlow(listOf<Bookmark>())
    val allBookmarks = _allBookmarks.asStateFlow()
    private val _bookmarkCount = MutableStateFlow(0)
    val bookmarkCount = _bookmarkCount.asStateFlow()

    fun observeLocalBookmarks(scope: CoroutineScope) = scope.launch(Dispatchers.IO) {
        bookmarkSource.getBookmarks().collect { bookmarks ->
            _allBookmarks.update {
                bookmarks.distinct().sortedByDescending { it.date_modified }
            }
            _bookmarkCount.emit(bookmarkSource.getBookmarkCount())
        }
    }

    fun fetchRemoteBookmarks(
        onRefreshStart: () -> Unit,
        onRefreshComplete: () -> Unit,
        onError: (APIError) -> Unit,
        scope: CoroutineScope
    ) = scope.launch(Dispatchers.IO) {
        api.waitForAuth()
        onRefreshStart()
        refreshAllBookmarksFromAPI(onError = onError)
        onRefreshComplete()
    }
}
