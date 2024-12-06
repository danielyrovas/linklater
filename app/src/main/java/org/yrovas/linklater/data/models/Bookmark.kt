package org.yrovas.linklater.data.models

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import linklater.BookmarkEntity
import linklater.GetBookmarkByIDwithTags
import linklater.GetBookmarkByURLwithTags
import linklater.GetBookmarksWithTags
import kotlin.time.ExperimentalTime

// A 1:1 representation of a LinkDing bookmark.
@Serializable
data class Bookmark(
    val id: Long,
    val url: String,
    val title: String? = null,
    val description: String? = null,
    val notes: String? = null,
    val website_title: String? = null,
    val website_description: String? = null,
//    val web_archive_snapshot_url: String? = null,
    val is_archived: Boolean = false,
    val unread: Boolean = false,
    val shared: Boolean = false,
    val date_added: String? = null,
    val date_modified: String? = null,
    @SerialName("tag_names") val tags: List<String> = emptyList(),
)

@OptIn(ExperimentalTime::class)
fun Bookmark.instantAdded() = Instant.parse(this.date_added!!)

@OptIn(ExperimentalTime::class)
fun Bookmark.instantModified() = this.date_modified?.let { Instant.parse(it) }

@OptIn(ExperimentalTime::class)
fun List<Bookmark>.sortByInstantAdded() : List<Bookmark> {
    return this.sortedBy { it.instantAdded() }
}

fun Bookmark.showTitleOrElse(value: String): String {
    return if (!title.isNullOrBlank()) {
        title
    } else if (!website_title.isNullOrBlank()) {
        website_title
    } else {
        value
    }
}

fun Bookmark.showDescriptionOrElse(value: String): String {
    return if (!description.isNullOrBlank()) {
        description
    } else if (!website_description.isNullOrBlank()) {
        website_description
    } else {
        value
    }
}

fun BookmarkEntity.toBookmark() = Bookmark(
    id = id,
    url = url,
    title = title,
    description = description,
    notes = notes,
    website_title = website_title,
    website_description = website_description,
    is_archived = is_archived ?: false,
    unread = unread ?: false,
    shared = shared ?: false,
    date_added = date_added,
    date_modified = date_modified,
)

fun GetBookmarksWithTags.toBookmark() = Bookmark(
    id = id,
    url = url,
    title = title,
    description = description,
    notes = notes,
    website_title = website_title,
    website_description = website_description,
    is_archived = is_archived ?: false,
    unread = unread ?: false,
    shared = shared ?: false,
    date_added = date_added,
    date_modified = date_modified,
    tags = tagNames.tryIntoTagList()
)

fun GetBookmarkByIDwithTags.toBookmark() = Bookmark(
    id = id,
    url = url,
    title = title,
    description = description,
    notes = notes,
    website_title = website_title,
    website_description = website_description,
    is_archived = is_archived ?: false,
    unread = unread ?: false,
    shared = shared ?: false,
    date_added = date_added,
    date_modified = date_modified,
    tags = tagNames.tryIntoTagList()
)

fun GetBookmarkByURLwithTags.toBookmark() = Bookmark(
    id = id,
    url = url,
    title = title,
    description = description,
    notes = notes,
    website_title = website_title,
    website_description = website_description,
    is_archived = is_archived ?: false,
    unread = unread ?: false,
    shared = shared ?: false,
    date_added = date_added,
    date_modified = date_modified,
    tags = tagNames.tryIntoTagList()
)

private fun String?.tryIntoTagList(): List<String> = this?.intoTagList() ?: emptyList()

private fun String.intoTagList(): List<String> = this.split(":: ::").filter { it.isNotBlank() }
