package org.yrovas.linklater.data.remote

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.AppScope
import org.yrovas.linklater.checkBookmarkAPIToken
import org.yrovas.linklater.checkURL
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.data.LocalBookmark
import org.yrovas.linklater.domain.APIError
import org.yrovas.linklater.domain.BookmarkAPI
import org.yrovas.linklater.domain.Err
import org.yrovas.linklater.domain.Ok
import org.yrovas.linklater.domain.Res

@AppScope
@Inject
class LinkDingAPI(
    private val client: HttpClient,
    private var endpoint: String? = null,
    private var token: String? = null,
    private val pageSize: Int = 20,
) : BookmarkAPI {
    init {
        Log.d("DEBUG/create", "LinkDingAPI: CREATE")
    }

    private val _authProvided = MutableStateFlow(false)
    override val authProvided: StateFlow<Boolean> = _authProvided.asStateFlow()

    override fun authenticate(
        endpoint: String?,
        token: String?,
    ): Res<Unit, APIError> {
        Log.d("DEBUG", "authenticate: with endpoint: $endpoint")
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

    override suspend fun getBookmarks(
        page: Int,
        query: String?,
    ): Res<List<Bookmark>, APIError> {
//        delay(3200)
        if (!authProvided.value) return Err(APIError.INCORRECT_AUTH)
        return try {
            Log.d("DEBUG/net", "getBookmarks: starting request")
            val response = client.get("${endpoint!!}/bookmarks/") {
                header("Authorization", "Token ${token!!}")
                if (page > 0) {
                    url.parameters.append(
                        "offset", (pageSize * page).toString()
                    )
                }
                if (!query.isNullOrBlank()) {
                    url.parameters.append("q", query)
                }
            }
            Ok(response.body<BookmarkResponse>().results)
        } catch (e: Exception) {
            Log.i("DEBUG/net", "getBookmarks: ${e.message}")
            Err(APIError.NO_CONNECTION)
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
            Log.d("DEBUG/net", "getTags: starting request")
            val response = client.get("${endpoint!!}/tags/") {
                header("Authorization", "Token ${token!!}")
                if (page > 0) {
                    url.parameters.append("offset", (pageSize * page).toString())
                }
            }
            Ok(response.body<TagResponse>().results)
        } catch (e: Exception) {
            Err(APIError.NO_CONNECTION)
        }
    }

//    @Serializable
//    data class BookmarkSavedResponse(
//        val id: Long,
//        val url: String,
//    )

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
