package org.yrovas.linklater.data.remote

import android.content.Context
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.data.LocalBookmark
import org.yrovas.linklater.domain.APIError
import org.yrovas.linklater.domain.Res

interface BookmarkAPI {
    suspend fun authenticate(endpoint: String? = null, token: String? = null, validate: Boolean = false): Res<Unit, APIError>
    suspend fun getBookmarks(page: Int, query: String? = null): Res<List<Bookmark>, APIError>
    suspend fun saveBookmark(bookmark: LocalBookmark): Res<Bookmark, APIError>
    suspend fun getTags(page: Int): Res<List<String>, APIError>
}
