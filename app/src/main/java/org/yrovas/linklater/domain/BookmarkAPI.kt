package org.yrovas.linklater.domain

import kotlinx.coroutines.flow.StateFlow
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.data.LocalBookmark
import org.yrovas.linklater.domain.APIError
import org.yrovas.linklater.domain.Res

interface BookmarkAPI {
    val authProvided: StateFlow<Boolean>
    fun authenticate(endpoint: String? = null, token: String? = null): Res<Unit, APIError>
    suspend fun checkConnection(): Res<Unit, APIError>
    suspend fun getBookmarks(page: Int, query: String? = null): Res<List<Bookmark>, APIError>
    suspend fun saveBookmark(bookmark: LocalBookmark): Res<Bookmark, APIError>
    suspend fun getTags(page: Int): Res<List<String>, APIError>
}
