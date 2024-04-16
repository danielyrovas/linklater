package org.yrovas.linklater.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// A datatype representing bookmarks which will be sent to the remote API.
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
    /// Returns a new local bookmark with the new values
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
