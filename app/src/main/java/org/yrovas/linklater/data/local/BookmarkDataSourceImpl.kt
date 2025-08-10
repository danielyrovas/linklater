package org.yrovas.linklater.data.local

import app.cash.sqldelight.TransactionWithoutReturn
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.AppScope
import org.yrovas.linklater.Database
import org.yrovas.linklater.Log
import org.yrovas.linklater.data.models.Bookmark
import org.yrovas.linklater.data.models.showTitleOrElse
import org.yrovas.linklater.data.models.toBookmark

@AppScope
@Inject
class BookmarkDataSourceImpl(db: Database) : BookmarkDataSource {
    init {
        Log.v { "Creating Bookmark DataSource" }
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
//            .map { it.toInt() }
    }

    // call from IO context AND within transaction
    // NOTE: this context receiver, currently prevents the use of
    //       transactionWithResult as the transaction block context
    context(TransactionWithoutReturn, CoroutineScope) private fun upsertBookmark(bookmark: Bookmark) {
        Log.v { "Inserting or updating bookmark: ${bookmark.showTitleOrElse(bookmark.url)}" }
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
            Log.v { "Deleting bookmark: $id" }
            q.deleteBookmarkByID(id)
        }
    }

    override suspend fun deleteWithinRange(
        startDate: String, endDate: String, exclude: List<Bookmark>?
    ) {
        withContext(Dispatchers.IO) {
            Log.v {
                "Deleting bookmarks within range: $startDate to $endDate " + "excluding: ${exclude?.joinToString { it.id.toString() }}"
            }
            q.transaction {
                if (exclude == null) q.deleteBookmarksCreatedWithinRange(startDate, endDate)
                else q.deleteBookmarksCreatedWithinRangeExcluding(
                    startDate, endDate, exclude.map { it.id })
            }
        }
    }

    override suspend fun upsertOrDeleteWithinRange(
        bookmarks: List<Bookmark>, startDate: String, endDate: String
    ) {
        if (bookmarks.isEmpty()) {
            deleteWithinRange(startDate, endDate)
            return
        }

        withContext(Dispatchers.IO) {
            val keepIds = bookmarks.map { it.id }
            q.transaction {
                if (bookmarks.size > 1) {
                    Log.v {
                        "Deleting bookmarks within range: $startDate to $endDate " + "excluding: ${keepIds.joinToString { it.toString() }}"
                    }
                    q.deleteBookmarksCreatedWithinRangeExcluding(
                        start_date = startDate, end_date = endDate, exclude = keepIds
                    )
                }
                bookmarks.forEach { upsertBookmark(it) }
            }
        }
    }
}
