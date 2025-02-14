package org.yrovas.linklater.data.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.data.BookmarkMetadata
import org.yrovas.linklater.data.LocalBookmark
import com.github.michaelbull.result.Result
import org.yrovas.linklater.data.models.APIError

interface BookmarkAPI {
    val authProvided: StateFlow<Boolean>
    fun authenticate(endpoint: String? = null, token: String? = null): Result<Unit, APIError>
    suspend fun checkConnection(): Result<Unit, APIError>
    suspend fun getBookmarks(page: Int): Result<List<Bookmark>, APIError>
    suspend fun getAllBookmarks(): Flow<Result<List<Bookmark>, APIError>>
    suspend fun saveBookmark(bookmark: LocalBookmark): Result<Bookmark, APIError>
    suspend fun getTags(page: Int): Result<List<String>, APIError>
    suspend fun checkExists(url: String): Pair<Bookmark?, BookmarkMetadata?>
}
