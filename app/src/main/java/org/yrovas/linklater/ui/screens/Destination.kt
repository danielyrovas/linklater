package org.yrovas.linklater.ui.screens

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Destination : NavKey {
    @Serializable
    data object Home : Destination

    @Serializable
    data object Logs : Destination
    @Serializable
    data object Preferences : Destination
    @Serializable
    data object SaveBookmark : Destination
    @Serializable
    data object SaveBookmarkActivity : Destination
}
