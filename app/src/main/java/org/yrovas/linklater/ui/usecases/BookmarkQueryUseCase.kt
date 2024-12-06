package org.yrovas.linklater.ui.usecases

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.data.models.Bookmark

@Inject
class BookmarkQueryUseCase() {
    fun filteredBookmarks(
        allBookmarks: StateFlow<List<Bookmark>>,
        queryFlow: Flow<String>,
        scope: CoroutineScope
    ): StateFlow<List<Bookmark>> = combine(
        allBookmarks,
        queryFlow,
    ) { bookmarks, query ->
        if (query.isBlank()) {
            bookmarks
        } else {
            bookmarks.filter { bookmarkMatchesQuery(it, query) }
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = allBookmarks.value
    )

    fun filteredBookmarkCount(
        filteredBookmarks: StateFlow<List<Bookmark>>,
        queryFlow: Flow<String>,
        scope: CoroutineScope
    ): StateFlow<Int> =
        combine(filteredBookmarks, queryFlow) { bookmarks, query ->
            bookmarks.size
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = filteredBookmarks.value.size
        )

    private fun bookmarkMatchesQuery(bookmark: Bookmark, query: String): Boolean {
        val stringWithoutTags = stringWithoutTags(query)
        val stringWithoutTagSymbols = stringWithoutTagSymbols(query)
        val partialTag = partialTagFromQuery(query)
        val tags = tagsFromQuery(query)

        return if (stringWithoutTags.isBlank()) {
            queryMatchesTags(bookmark, tags, partialTag)
        } else {
            queryMatchesTags(bookmark, tags, partialTag) && (queryMatchesString(
                bookmark, query
            ) || queryMatchesString(
                bookmark, stringWithoutTags
            ) || queryMatchesString(
                bookmark, stringWithoutTagSymbols
            ))
        }
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
}
