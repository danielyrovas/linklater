package org.yrovas.linklater.ui.screens

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.yrovas.linklater.ui.screens.saveBookmark.BookmarkParam

sealed interface Destination : NavKey {
    @Serializable
    data object Home : Destination

    @Serializable
    data object Logs : Destination

    @Serializable
    data object Preferences : Destination

    @Serializable
    data class SaveBookmark(val param: BookmarkParam = BookmarkParam.New) : Destination

    @Serializable
    data object SaveBookmarkActivity : Destination
}
