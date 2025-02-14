package org.yrovas.linklater.ui.screens

import kotlinx.serialization.Serializable

sealed interface Destination {
    @Serializable
    data object Home : Destination
    @Serializable
    data object Preferences : Destination
    @Serializable
    data object SaveBookmark : Destination
    @Serializable
    data object SaveBookmarkActivity : Destination
}
