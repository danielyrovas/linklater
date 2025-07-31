package org.yrovas.linklater.data.local

import kotlinx.coroutines.flow.Flow
import org.yrovas.linklater.AppScope
import org.yrovas.linklater.data.models.Bookmark

interface BookmarkDataSource {
    suspend fun getBookmark(id: Long): Bookmark?
    fun getBookmarks(): Flow<List<Bookmark>>
    fun getBookmarkCount(): Int
    suspend fun insertBookmark(bookmark: Bookmark)
    suspend fun insertBookmarks(bookmarks: List<Bookmark>)
    suspend fun deleteBookmark(id: Long)
    suspend fun upsertOrDeleteWithinRange(
        bookmarks: List<Bookmark>,
        startDate: String,
        endDate: String,
    )
    suspend fun deleteWithinRange(
        startDate: String,
        endDate: String,
        exclude: List<Bookmark>? = null
    )
}
