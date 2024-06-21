package org.yrovas.linklater.data.local

import android.util.Log
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.TransactionWithReturn
import app.cash.sqldelight.TransactionWithoutReturn
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.yrovas.linklater.Database
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.data.showTitleOrElse
import org.yrovas.linklater.data.toBookmark
import org.yrovas.linklater.domain.BookmarkDataSource

const val TAG = "DEBUG"

class BookmarkDataSourceImpl(db: Database) : BookmarkDataSource {
    init {
        Log.d("DEBUG/create", "BookmarkDataSourceImpl: CREATE")
    }

    private val q = db.bookmarkTagsQueries

    override suspend fun getBookmark(id: Long): Bookmark? {
        return withContext(Dispatchers.IO) {
            q.getBookmarkByIDwithTags(id).executeAsOneOrNull()?.toBookmark()
        }
    }

    override fun getBookmarks(): Flow<List<Bookmark>> {
        return q.getBookmarksWithTags().asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toBookmark() }
        }
    }

    override fun getBookmarkCount(): Int {
        return q.countBookmarks().executeAsOneOrNull()?.toInt() ?: -1
//            .map { Log.d(TAG, "getBookmarkCount: EMIT $it"); it.toInt() }
    }

    // call from IO context AND within transaction
    // NOTE: this context receiver, currently prevents the use of
    //       transactionWithResult as the transaction block context
    context(TransactionWithoutReturn, CoroutineScope)
    private fun upsertBookmark(bookmark: Bookmark) {
        Log.d(TAG, "upsertBOOKMARK: ${bookmark.title} ${bookmark.website_title}")
        Log.d(TAG, "upsertTAGS: ${bookmark.tags}")
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
            q.insertTagForBookmark(bookmarkID = id, tagID = tagID)
        }
    }

    override suspend fun insertBookmark(bookmark: Bookmark) {
        withContext(Dispatchers.IO) {
            q.transaction {
                upsertBookmark(bookmark)
            }
        }
    }

    override suspend fun insertBookmarks(bookmarks: List<Bookmark>) {
        withContext(Dispatchers.IO) {
            q.transaction {
                bookmarks.forEach { upsertBookmark(it) }
            }
        }
    }

    override suspend fun deleteBookmark(id: Long) {
        withContext(Dispatchers.IO) {
            q.deleteBookmarkByID(id)
        }
    }

    override suspend fun deleteWithinRange(
        startDate: String, endDate: String, exclude: List<Bookmark>?
    ) {
        withContext(Dispatchers.IO) {
            q.transaction {
                if (exclude == null) q.deleteBookmarksCreatedWithinRange(startDate, endDate)
                else q.deleteBookmarksCreatedWithinRangeExcluding(
                    startDate, endDate, exclude.map { it.id }
                )
            }
        }
    }

    override suspend fun upsertOrDeleteWithinRange(
        bookmarks: List<Bookmark>, startDate: String, endDate: String
    ) {
        if (bookmarks.isEmpty()) {
            Log.d(TAG, "upsertOrDeleteWithinRange: NO BOOKMARKS")
            deleteWithinRange(startDate, endDate)
            return
        }

        withContext(Dispatchers.IO) {
            val keepIds = bookmarks.map { it.id }
            q.transaction {
                if (bookmarks.size > 1) q.deleteBookmarksCreatedWithinRangeExcluding(
                    start_date = startDate, end_date = endDate, exclude = keepIds
                )
                bookmarks.forEach { upsertBookmark(it) }
            }
        }
    }
}
