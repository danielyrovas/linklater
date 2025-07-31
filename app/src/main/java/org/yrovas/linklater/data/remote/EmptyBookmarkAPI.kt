package org.yrovas.linklater.data.remote

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.yrovas.linklater.data.models.Bookmark
import org.yrovas.linklater.data.models.BookmarkMetadata
import org.yrovas.linklater.data.models.LocalBookmark
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Err
import org.yrovas.linklater.data.models.APIError

class EmptyBookmarkAPI : BookmarkAPI {
    override fun authenticate(
        endpoint: String?,
        token: String?,
    ): Result<Unit, APIError> {
        return Ok(Unit)
    }

    override suspend fun checkConnection(): Result<Unit, APIError> {
        return Ok(Unit)
    }

    override val authProvided: StateFlow<Boolean>
        get() = MutableStateFlow(false)

    override suspend fun getBookmarks(
        page: Int,
    ): Result<List<Bookmark>, APIError> {
        delay(4300)
        return Ok(emptyList())
//        return Err(APIError.AUTH)
    }

    override suspend fun getAllBookmarks(): Flow<Result<List<Bookmark>, APIError>> {
        return flowOf()
    }

    override suspend fun saveBookmark(bookmark: LocalBookmark): Result<Bookmark, APIError> {
        return Err(APIError.NO_CONNECTION)
    }

    override suspend fun getTags(page: Int): Result<List<String>, APIError> {
        return Ok(emptyList())
    }

    override suspend fun checkExists(url: String): Pair<Bookmark?, BookmarkMetadata?> {
        return null to null
    }
}
