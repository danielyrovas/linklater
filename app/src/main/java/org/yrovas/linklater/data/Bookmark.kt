package org.yrovas.linklater.data

import kotlinx.serialization.*

// A 1:1 representation of a LinkDing bookmark.
@Serializable
data class Bookmark(
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

// A datatype representing bookmarks which have not been
// sent to the remote API yet.
// This is the datatype used to send bookmarks to the
// remote API.
@Serializable
data class LocalBookmark(
    var url: String,
    var title: String? = null,
    var description: String? = null,
    var notes: String? = null,
    var is_archived: Boolean = false,
    var unread: Boolean = false,
    var shared: Boolean = false,
    @SerialName("tag_names") var tags: List<String> = emptyList(),
) {

    /// Returns a new local bookmark with the new values if provided
    fun withUpdates(
        url: String? = null,
        title: String? = null,
        description: String? = null,
        notes: String? = null,
        is_archived: Boolean? = null,
        unread: Boolean? = null,
        shared: Boolean? = null,
        tags: List<String>? = null,
    ): LocalBookmark {
        return LocalBookmark(
            url = url?.ifBlank { null } ?: url ?: this.url,
            title = title?.ifBlank { null } ?: title ?: this.title,
            description = description?.ifBlank { null } ?: description ?: this.description,
            notes = notes?.ifBlank { null } ?: notes ?: this.notes,
            is_archived = is_archived ?: this.is_archived,
            unread = unread ?: this.unread,
            shared = shared ?: this.shared,
            tags = tags ?: this.tags
        )
    }
}
