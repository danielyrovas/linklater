package org.yrovas.linklater.data.local

import kotlinx.coroutines.flow.Flow
import org.yrovas.linklater.data.Bookmark

interface BookmarkDataSource {
    suspend fun getBookmark(id: Long): Bookmark?
    fun getBookmarks(): Flow<List<Bookmark>>
    suspend fun insertBookmark(bookmark: Bookmark)
    suspend fun insertBookmarks(bookmarks: List<Bookmark>)
    suspend fun deleteBookmark(id: Long)
}
