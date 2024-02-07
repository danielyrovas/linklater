package org.yrovas.linklater

import android.content.Context
import org.yrovas.linklater.data.*

interface BookmarkAPI {
    suspend fun getBookmarks(page: Int, query: String? = null): Result<List<Bookmark>>
    suspend fun getCachedBookmarks(context: Context): List<Bookmark>
    suspend fun saveBookmark(bookmark: LocalBookmark): Boolean
    suspend fun cacheBookmarks(context: Context, bookmarks: List<Bookmark>)
    suspend fun getCachedTags(context: Context): List<String>
    suspend fun cacheTags(context: Context, tags: List<String>)
    suspend fun getTags(page: Int): Result<List<String>>
}
