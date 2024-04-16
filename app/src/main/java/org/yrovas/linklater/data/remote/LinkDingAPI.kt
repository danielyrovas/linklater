package org.yrovas.linklater.data.remote

import android.content.Context
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.AppScope
import org.yrovas.linklater.checkBookmarkAPIToken
import org.yrovas.linklater.checkURL
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.data.LocalBookmark
import org.yrovas.linklater.domain.APIError
import org.yrovas.linklater.domain.Err
import org.yrovas.linklater.domain.Ok
import org.yrovas.linklater.domain.Res
import org.yrovas.linklater.domain.toRes
import java.io.File

@AppScope
@Inject
class LinkDingAPI(
    private val client: HttpClient,
    private var endpoint: String? = null,
    private var token: String? = null,
    private val pageSize: Int = 20,
) : BookmarkAPI {
    init {
        Log.d("DEBUG", "CREATING BOOKMARK API: ")
    }

    private val authProvided
        get() = !endpoint.isNullOrBlank() && !token.isNullOrBlank()

    override suspend fun authenticate(
        endpoint: String?,
        token: String?,
        validate: Boolean,
    ): Res<Unit, APIError> {
        Log.d("DEBUG", "authenticate: with endpoint: $endpoint")
        if (!endpoint.isNullOrBlank()) {
            if (!checkURL(endpoint)) return Err(APIError.AUTH)
            this.endpoint = endpoint
        }
        if (!token.isNullOrBlank()) {
            if (!checkBookmarkAPIToken(token)) return Err(APIError.AUTH)
            this.token = token
        }

        if (!validate) return Ok(Unit)

        if (!authProvided) return Err(APIError.AUTH)

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
        if (!authProvided) return Err(APIError.AUTH)
        return runCatching {
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
//            Json.decodeFromString<BookmarkResponse>(response.bodyAsText()).results
            response.body<BookmarkResponse>().results // ?? very kool Ktor
        }.onFailure {
            Log.i("DEBUG/net", "getBookmarks: ${it.message}")
            Result.failure<List<Bookmark>>(it)
        }.toRes(withError = APIError.CONNECTION)
    }

    override suspend fun saveBookmark(bookmark: LocalBookmark): Res<Bookmark, APIError> {
        if (!authProvided) return Err(APIError.AUTH)
        return try {
            val response = client.post("${endpoint!!}/bookmarks/") {
                setBody(bookmark)
                header("Authorization", "Token ${token!!}")
            }
            when (response.status.value) {
                in 200..299 -> Ok(response.body<Bookmark>())
                else -> Err(APIError.AUTH)
            }
        } catch (e: Exception) {
            Err(APIError.CONNECTION)
        }
    }

    override suspend fun getTags(page: Int): Res<List<String>, APIError> {
        if (!authProvided) return Err(APIError.AUTH)
        return runCatching {
            Log.d("DEBUG/net", "getTags: starting request")
            val response = client.get("${endpoint!!}/tags/") {
                header("Authorization", "Token ${token!!}")
                if (page > 0) {
                    url.parameters.append("offset", (pageSize * page).toString())
                }
            }
            response.body<TagResponse>().results
        }.onFailure {
            Log.i("DEBUG/net", "getTags: ${it.message}")
            Result.failure<List<String>>(it)
        }.toRes(APIError.CONNECTION)
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
