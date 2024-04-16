package org.yrovas.linklater.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.yrovas.linklater.data.Bookmark

class EmptyBookmarkSource : BookmarkDataSource {
    override suspend fun getBookmark(id: Long): Bookmark? {
        return null
    }

    override fun getBookmarks(): Flow<List<Bookmark>> {
        return emptyList<List<Bookmark>>().asFlow()
    }

    override suspend fun insertBookmark(bookmark: Bookmark) {}
    override suspend fun insertBookmarks(bookmarks: List<Bookmark>) {}
    override suspend fun deleteBookmark(id: Long) {}
}
