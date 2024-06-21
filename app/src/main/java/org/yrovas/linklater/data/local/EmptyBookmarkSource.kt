package org.yrovas.linklater.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.domain.BookmarkDataSource

class EmptyBookmarkSource : BookmarkDataSource {
    override suspend fun getBookmark(id: Long): Bookmark? {
        return null
    }

    override fun getBookmarks(): Flow<List<Bookmark>> {
        return emptyList<List<Bookmark>>().asFlow()
    }

    override fun getBookmarkCount(): Int {
        return 0
    }

    override suspend fun insertBookmark(bookmark: Bookmark) {}
    override suspend fun insertBookmarks(bookmarks: List<Bookmark>) {}
    override suspend fun deleteBookmark(id: Long) {}
    override suspend fun upsertOrDeleteWithinRange(
        bookmarks: List<Bookmark>, startDate: String, endDate: String
    ) {
    }

    override suspend fun deleteWithinRange(
        startDate: String,
        endDate: String,
        exclude: List<Bookmark>?
    ) {
    }

}
