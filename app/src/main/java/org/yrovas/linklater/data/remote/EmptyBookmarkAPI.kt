package org.yrovas.linklater.domain

import android.content.Context
import kotlinx.coroutines.delay
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.data.LocalBookmark

class EmptyBookmarkAPI : BookmarkAPI {
    override suspend fun authenticate(
        endpoint: String?,
        token: String?,
        validate: Boolean,
    ): Res<Unit, APIError> {
        return Ok(Unit)
    }

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

    override suspend fun saveBookmark(bookmark: LocalBookmark): Res<Int, APIError> {
        return Ok(200)
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
