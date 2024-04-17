package org.yrovas.linklater.data.local

import android.util.Log
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.yrovas.linklater.Database
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.data.toBookmark

const val TAG = "DEBUG"

class BookmarkDataSourceImpl(db: Database) : BookmarkDataSource {
    private val q = db.bookmarkTagsQueries

    override suspend fun getBookmark(id: Long): Bookmark? {
        return withContext(Dispatchers.IO) {
            q.getBookmarkByIDwithTags(id).executeAsOneOrNull()?.toBookmark()
        }
    }

    override fun getBookmarks(): Flow<List<Bookmark>> {
        return q.getBookmarksWithTags().asFlow().mapToList(Dispatchers.IO)
            .map { list ->
                list.map { it.toBookmark() }
        }
    }

    override suspend fun insertBookmarks(bookmarks: List<Bookmark>) {
        bookmarks.forEach {
            insertBookmark(it)
        }
    }

    override suspend fun insertBookmark(bookmark: Bookmark) {
        Log.d(
            TAG, "insertBOOKMARK: ${bookmark.title} ${bookmark.website_title}"
        )
        Log.d(
            TAG, "insertTAGS: ${bookmark.tags}"
        )
        withContext(Dispatchers.IO) {
            q.transaction {
                val id = q.insertBookmark(
                    id = bookmark.id,
                    url = bookmark.url,
                    title = bookmark.title,
                    description = bookmark.description,
                    notes = bookmark.notes,
                    website_title = bookmark.website_title,
                    website_description = bookmark.website_description,
                    is_archived = bookmark.is_archived,
                    unread = bookmark.unread,
                    shared = bookmark.shared,
                    date_added = bookmark.date_added,
                    date_modified = bookmark.date_modified
                ).executeAsOneOrNull()!!
                bookmark.tags.forEach {
                    q.insertTag(name = it)
                    val tagID = q.getTagByName(it).executeAsOneOrNull()!!
                    Log.d(TAG, "inserted TAG: $it,$tagID")
                    q.insertTagForBookmark(
                        bookmarkID = id,
                        tagID = tagID,
                    )
                }
            }
        }
    }

    override suspend fun deleteBookmark(id: Long) {
        withContext(Dispatchers.IO) {
            q.deleteBookmarkByID(id)
        }
    }
}
