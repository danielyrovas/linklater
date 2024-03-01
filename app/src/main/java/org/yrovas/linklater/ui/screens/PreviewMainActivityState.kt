package org.yrovas.linklater.ui.screens

import android.content.Context
import kotlinx.coroutines.flow.update
import org.yrovas.linklater.MainActivityViewModel
import org.yrovas.linklater.data.Bookmark

class PreviewMainActivityState : MainActivityViewModel() {
    fun setBookmarks(bookmarks: List<Bookmark>) {
        _displayedBookmarks.update {
            bookmarks
        }
    }

    override suspend fun doRefresh(context: Context) {}
}
