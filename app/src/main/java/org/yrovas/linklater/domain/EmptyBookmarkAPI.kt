package org.yrovas.linklater

import android.content.Context
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.data.LocalBookmark
import org.yrovas.linklater.domain.*

class EmptyBookmarkAPI : BookmarkAPI {
    override suspend fun getBookmarks(
        page: Int,
        query: String?,
    ): Res<List<Bookmark>, APIError> {
//        return Ok(emptyList())
        return Err(APIError.AUTH)
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

    override suspend fun getTags(page: Int): Res<List<String>, APIError> {
        return Ok(emptyList())
    }
}
