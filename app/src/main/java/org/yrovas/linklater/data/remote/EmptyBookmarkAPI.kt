package org.yrovas.linklater.data.remote

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.data.LocalBookmark
import org.yrovas.linklater.domain.APIError
import org.yrovas.linklater.domain.BookmarkAPI
import org.yrovas.linklater.domain.Err
import org.yrovas.linklater.domain.Ok
import org.yrovas.linklater.domain.Res

class EmptyBookmarkAPI : BookmarkAPI {
    override fun authenticate(
        endpoint: String?,
        token: String?,
    ): Res<Unit, APIError> {
        return Ok(Unit)
    }

    override suspend fun checkConnection(): Res<Unit, APIError> {
        return Ok(Unit)
    }

    override val authProvided: StateFlow<Boolean>
        get() = MutableStateFlow(false)

    override suspend fun getBookmarks(
        page: Int,
        query: String?,
    ): Res<List<Bookmark>, APIError> {
        delay(4300)
        return Ok(emptyList())
//        return Err(APIError.AUTH)
    }

    override suspend fun saveBookmark(bookmark: LocalBookmark): Res<Bookmark, APIError> {
        return Err(APIError.NO_CONNECTION)
    }

    override suspend fun getTags(page: Int): Res<List<String>, APIError> {
        return Ok(emptyList())
    }
}
