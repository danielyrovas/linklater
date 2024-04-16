package org.yrovas.linklater.data.remote

import android.content.Context
import kotlinx.coroutines.delay
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.data.LocalBookmark
import org.yrovas.linklater.domain.APIError
import org.yrovas.linklater.domain.Err
import org.yrovas.linklater.domain.Ok
import org.yrovas.linklater.domain.Res

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
        delay(4300)
        return Ok(emptyList())
//        return Err(APIError.AUTH)
    }

    override suspend fun saveBookmark(bookmark: LocalBookmark): Res<Bookmark, APIError> {
        return Err(APIError.CONNECTION)
    }

    override suspend fun getTags(page: Int): Res<List<String>, APIError> {
        return Ok(emptyList())
    }
}
