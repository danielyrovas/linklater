package org.yrovas.linklater

import android.content.Context
import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
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
    ): Result<List<Bookmark>> {
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
        }
    }

    override suspend fun saveBookmark(bookmark: LocalBookmark): Boolean {
        return Ktor.client.post("$endpoint/bookmarks/") {
            setBody(bookmark)
            header("Authorization", "Token $token")
        }.status.value in 200..299
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

    override suspend fun getTags(page: Int): Result<List<String>> {
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
            Result.failure<List<String>>(it)
        }
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
}
