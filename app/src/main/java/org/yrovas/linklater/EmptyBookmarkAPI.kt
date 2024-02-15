package org.yrovas.linklater

import android.content.Context
import androidx.annotation.Keep
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.data.LocalBookmark

@Keep
class EmptyBookmarkAPI : BookmarkAPI {
    override suspend fun getBookmarks(
        page: Int,
        query: String?,
    ): Result<List<Bookmark>> {
        return Result.success(emptyList())
    }

    override suspend fun getCachedBookmarks(context: Context): List<Bookmark> {
        return emptyList()
    }

    override suspend fun saveBookmark(bookmark: LocalBookmark): Boolean {
        return true
    }

    override suspend fun cacheBookmarks(
        context: Context,
        bookmarks: List<Bookmark>,
    ) {
    }

    override suspend fun getCachedTags(context: Context): List<String> {
        return emptyList()
    }

    override suspend fun cacheTags(context: Context, tags: List<String>) {}

    override suspend fun getTags(page: Int): Result<List<String>> {
        return Result.success(emptyList())
    }
}
