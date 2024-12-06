package org.yrovas.linklater.data.models

import kotlinx.serialization.Serializable

@Serializable
data class BookmarkMetadata(
    val url: String,
    val title: String? = null,
    val description: String? = null,
)
