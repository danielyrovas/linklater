package org.yrovas.linklater.domain

import android.content.Context
import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.data.LocalBookmark
import java.io.File

const val BOOKMARKS_CACHE_PATH = "bookmark_page_cache.json"
const val TAGS_CACHE_PATH = "tags_cache.json"
const val TAG = "DEBUG"

class LinkDingAPI(
    private var endpoint: String,
    private var token: String,
    private val pageSize: Int = 20,
) : BookmarkAPI {
    override suspend fun getBookmarks(
        page: Int,
        query: String?,
    ): Res<List<Bookmark>, APIError> {
        return runCatching {
            Log.d("DEBUG/net", "getBookmarks: starting request")
            val response = Ktor.client.get("$endpoint/bookmarks/") {
                header("Authorization", "Token $token")
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
            Log.i(TAG, "getBookmarks: ${it.message}")
            Result.failure<List<Bookmark>>(it)
        }.toRes(withError = APIError.CONNECTION)
    }

    override suspend fun saveBookmark(bookmark: LocalBookmark): Res<Int, APIError> {
        return try {
            val status = Ktor.client.post("$endpoint/bookmarks/") {
                setBody(bookmark)
                header("Authorization", "Token $token")
            }.status.value
            when (status) {
                in 200..299 -> Ok(status)
                else -> Err(APIError.AUTH)
            }
        } catch (e: Exception) {
            Err(APIError.CONNECTION)
        }
    }

    override suspend fun getCachedBookmarks(context: Context): List<Bookmark> {
        val cache = File(context.cacheDir, BOOKMARKS_CACHE_PATH)
        return runCatching {
            Json.decodeFromString<List<Bookmark>>(cache.readText())
        }.getOrElse {
            cache.createNewFile()
            emptyList()
        }
    }

    override suspend fun cacheBookmarks(
        context: Context,
        bookmarks: List<Bookmark>,
    ) {
        File(context.cacheDir, BOOKMARKS_CACHE_PATH).writeText(
            Json.encodeToJsonElement(bookmarks).toString()
        )
    }

    override suspend fun getTags(page: Int): Res<List<String>, APIError> {
        return runCatching {
            Log.d("DEBUG/net", "getTags: starting request")
            val response = Ktor.client.get("$endpoint/tags/") {
                header("Authorization", "Token $token")
                if (page > 0) {
                    url.parameters.append("offset", (pageSize * page).toString())
                }
            }
            response.body<TagResponse>().results
        }.onFailure {
            Log.i(TAG, "getTags: ${it.message}")
            Result.failure<List<String>>(it)
        }.toRes(APIError.CONNECTION)
    }

    override suspend fun getCachedTags(context: Context): List<String> {
        val cache = File(context.cacheDir, TAGS_CACHE_PATH)
        return runCatching {
            Json.decodeFromString<List<String>>(cache.readText())
        }.getOrElse { cache.createNewFile(); emptyList() }
    }

    override suspend fun cacheTags(context: Context, tags: List<String>) {
        // TODO: add date_modified and usage count to Tag cache
        File(context.cacheDir, TAGS_CACHE_PATH).writeText(
            Json.encodeToJsonElement(tags).toString()
        )
    }

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

    @Serializable
    data class LinkDingBookmark(
        val id: Int,
        val url: String,
        val title: String? = null,
        val description: String? = null,
        val notes: String? = null,
        val website_title: String? = null,
        val website_description: String? = null,
        val is_archived: Boolean = false,
        val unread: Boolean = false,
        val shared: Boolean = false,
        val date_added: String? = null,
        val date_modified: String? = null,
        @SerialName("tag_names") val tags: List<String> = emptyList(),
    )
}
