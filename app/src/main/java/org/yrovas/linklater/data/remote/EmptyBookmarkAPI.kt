package org.yrovas.linklater.data.remote

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.data.BookmarkMetadata
import org.yrovas.linklater.data.LocalBookmark
import org.yrovas.linklater.Err
import org.yrovas.linklater.Ok
import org.yrovas.linklater.Res

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
    ): Res<List<Bookmark>, APIError> {
        delay(4300)
        return Ok(emptyList())
//        return Err(APIError.AUTH)
    }

    override suspend fun getAllBookmarks(): Flow<Res<List<Bookmark>, APIError>> {
        return flowOf()
    }

    override suspend fun saveBookmark(bookmark: LocalBookmark): Res<Bookmark, APIError> {
        return Err(APIError.NO_CONNECTION)
    }

    override suspend fun getTags(page: Int): Res<List<String>, APIError> {
        return Ok(emptyList())
    }

    override suspend fun checkExists(url: String): Pair<Bookmark?, BookmarkMetadata?> {
        return null to null
    }
}
