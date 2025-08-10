package org.yrovas.linklater.data.remote

import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import org.yrovas.linklater.Log
import org.yrovas.linklater.data.models.APIError
import org.yrovas.linklater.data.models.Bookmark
import org.yrovas.linklater.data.models.BookmarkMetadata
import org.yrovas.linklater.data.models.LocalBookmark

interface BookmarkAPI {
    val authProvided: StateFlow<Boolean>

    suspend fun waitForAuth() {
        Log.d { "Waiting for auth..." }
        authProvided.first { it }
    }

    fun authenticate(endpoint: String? = null, token: String? = null): Result<Unit, APIError>
    suspend fun checkConnection(): Result<Unit, APIError>
    suspend fun getBookmarks(page: Int): Result<List<Bookmark>, APIError>
    suspend fun getAllBookmarks(): Flow<Result<List<Bookmark>, APIError>>
    suspend fun saveBookmark(bookmark: LocalBookmark): Result<Bookmark, APIError>
    suspend fun getTags(page: Int): Result<List<String>, APIError>
    suspend fun checkExists(url: String): Pair<Bookmark?, BookmarkMetadata?>
}
