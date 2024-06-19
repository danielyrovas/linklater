package org.yrovas.linklater.data.remote

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.AppScope
import org.yrovas.linklater.checkBookmarkAPIToken
import org.yrovas.linklater.checkURL
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.data.BookmarkMetadata
import org.yrovas.linklater.data.LocalBookmark
import org.yrovas.linklater.domain.APIError
import org.yrovas.linklater.domain.BookmarkAPI
import org.yrovas.linklater.domain.Err
import org.yrovas.linklater.domain.Ok
import org.yrovas.linklater.domain.Res
import org.yrovas.linklater.domain.getOrThrow
import org.yrovas.linklater.domain.ifOk
import org.yrovas.linklater.domain.isOk
import org.yrovas.linklater.domain.mapData
import org.yrovas.linklater.domain.ok
import org.yrovas.linklater.domain.then

const val TAG = "DEBUG/net"
const val MAX_PAGE_COUNT = 10000

@AppScope
@Inject
class LinkDingAPI(
    private val client: HttpClient,
    private var endpoint: String? = null,
    private var token: String? = null,
    private val pageSize: Int = 100,
) : BookmarkAPI {
    private val _authProvided = MutableStateFlow(false)
    override val authProvided: StateFlow<Boolean> = _authProvided.asStateFlow()

    override fun authenticate(
        endpoint: String?,
        token: String?,
    ): Res<Unit, APIError> {
        Log.d(TAG, "authenticate: with endpoint: $endpoint")
        if (!endpoint.isNullOrBlank()) {
            if (!checkURL(endpoint)) return Err(APIError.INCORRECT_ENDPOINT)
            this.endpoint = endpoint
        }
        if (!token.isNullOrBlank()) {
            if (!checkBookmarkAPIToken(token)) return Err(APIError.INCORRECT_AUTH)
            this.token = token
        }

        _authProvided.update { !endpoint.isNullOrBlank() && !token.isNullOrBlank() }
        if (!authProvided.value) return Err(APIError.INCORRECT_AUTH)
        return Ok(Unit)
    }

    override suspend fun checkConnection(): Res<Unit, APIError> {
        if (!authProvided.value) return Err(APIError.INCORRECT_AUTH)

        return when (val res = getBookmarks(page = 0)) {
            is Res.Err -> Err(res.error)
            is Res.Ok -> Ok(Unit)
        }
    }

    private suspend fun fetchBookmarks(
        page: Int,
        archived: Boolean = false,
        sortByAddedAsc: Boolean = false,
    ): Res<BookmarkResponse, APIError> {
        if (!authProvided.value) return Err(APIError.INCORRECT_AUTH)
        return try {
            val response =
                client.get("${endpoint!!}/bookmarks/${if (archived) "archived/" else ""}") {
                    header("Authorization", "Token ${token!!}")
                    if (sortByAddedAsc) {
                        parameter("sort", "added_asc")
                    }
                    if (page > 0) {
                        parameter("offset", (pageSize * page).toString())
                    }
                }
            Ok(response.body<BookmarkResponse>())
        } catch (e: Exception) {
            Log.i(TAG, "getBookmarks: ${e.message}")
            Err(APIError.NO_CONNECTION)
        }
    }

    override suspend fun getBookmarks(page: Int): Res<List<Bookmark>, APIError> {
        return fetchBookmarks(page).mapData { it.results }
    }

    override suspend fun getAllBookmarks(): Flow<Res<List<Bookmark>, APIError>> {
        return flow {
            Log.d(TAG, "getAllBookmarks: Flow Created")
            var page = 0
            var archived = false

            // NOTE: we might not crawl archived pages if there are more than 1000 x 10000 bookmarks
            while (page < MAX_PAGE_COUNT) {
                Log.d(TAG, "getAllBookmarks: Fetching page $page")
                val res = fetchBookmarks(page, archived, sortByAddedAsc = true)
                page++
                if (res.isOk) {
                    emit(res.mapData { it.results })

                    // exit when finished archived
                    if (res.getOrThrow().next.isNullOrBlank() && archived) break

                    // crawl archived after completing unarchived.
                    if (res.getOrThrow().next.isNullOrBlank() && !archived) {
                        archived = true
                        page = 0
                    }
                } else {
                    break
                }
            }
        }
    }

    override suspend fun saveBookmark(bookmark: LocalBookmark): Res<Bookmark, APIError> {
        if (!authProvided.value) return Err(APIError.INCORRECT_AUTH)
        return try {
            val response = client.post("${endpoint!!}/bookmarks/") {
                setBody(bookmark)
                header("Authorization", "Token ${token!!}")
            }
            when (response.status.value) {
                in 200..299 -> Ok(response.body<Bookmark>())
                else -> Err(APIError.INCORRECT_AUTH)
            }
        } catch (e: Exception) {
            Err(APIError.NO_CONNECTION)
        }
    }

    override suspend fun getTags(page: Int): Res<List<String>, APIError> {
        if (!authProvided.value) return Err(APIError.INCORRECT_AUTH)
        return try {
            Log.d(TAG, "getTags: starting request")
            val response = client.get("${endpoint!!}/tags/") {
                header("Authorization", "Token ${token!!}")
                if (page > 0) {
                    parameter("offset", (pageSize * page).toString())
                }
            }
            Ok(response.body<TagResponse>().results)
        } catch (e: Exception) {
            Err(APIError.NO_CONNECTION)
        }
    }

    override suspend fun checkExists(url: String): Pair<Bookmark?, BookmarkMetadata?> {
        if (!authProvided.value) return null to null
        if (url.isBlank()) return null to null

        return try {
            Log.d(TAG, "checkExists")
            val response = client.get("${endpoint!!}/bookmarks/check/") {
                header("Authorization", "Token ${token!!}")
                parameter("url", url)
            }
            val r = response.body<BookmarkExistsResponse>()
            r.bookmark to r.metadata
        } catch (e: Exception) {
            null to null
        }
    }

    @Serializable
    data class BookmarkExistsResponse(
        val bookmark: Bookmark?,
        val metadata: BookmarkMetadata,
    )

    @Serializable
    data class BookmarkResponse(
        val count: Int,
        val next: String?,
        val previous: String?,
        val results: List<Bookmark>,
    )

    @Serializable
    data class TagResponse(
        val count: Int,
        val next: String?,
        val previous: String?,
        val results: List<String>,
    )
}
