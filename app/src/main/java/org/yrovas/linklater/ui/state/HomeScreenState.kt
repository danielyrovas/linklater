package org.yrovas.linklater.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.AppViewModel
import org.yrovas.linklater.domain.BookmarkAPI
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.domain.*

@Inject
class HomeScreenState(private val api: BookmarkAPI) : ViewModel() {
    fun refreshBookmarks(onRefresh: suspend (Res<Any, APIError>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshing.update { true }
            val res = api.getBookmarks(page = 0)
            res.ok { setDisplayedBookmarks(it) }
            _isRefreshing.update { false }
            onRefresh(res)
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    var isRefreshing = _isRefreshing.asStateFlow()

    private val _displayedBookmarks = MutableStateFlow(listOf<Bookmark>())
    val displayedBookmarks = _displayedBookmarks.asStateFlow()

    fun setDisplayedBookmarks(bookmarks: List<Bookmark>) {
        _displayedBookmarks.update { bookmarks }
    }
}
