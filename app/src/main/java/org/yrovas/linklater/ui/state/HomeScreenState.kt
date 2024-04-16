package org.yrovas.linklater.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.data.local.BookmarkDataSource
import org.yrovas.linklater.data.remote.BookmarkAPI
import org.yrovas.linklater.domain.APIError
import org.yrovas.linklater.domain.Res
import org.yrovas.linklater.domain.ifOk

@Inject
class HomeScreenState(
    private val api: BookmarkAPI,
    private val bookmarkSource: BookmarkDataSource,
) : ViewModel() {
    fun refreshBookmarks(onRefresh: suspend (Res<Any, APIError>) -> Unit) {
        _isRefreshing.update { true }
        _hasRefreshed.update { true }
        viewModelScope.launch(Dispatchers.IO) {
            val res = api.getBookmarks(page = 0)
//            res.ok { setDisplayedBookmarks(it) }
            _isRefreshing.update { false }
            onRefresh(res)

            res.ifOk {
                bookmarkSource.insertBookmark(it.first())
            }
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    var isRefreshing = _isRefreshing.asStateFlow()

    private val _hasRefreshed = MutableStateFlow(false)
    var hasRefreshed = _hasRefreshed.asStateFlow()

    private val _displayedBookmarks = MutableStateFlow(listOf<Bookmark>())
    val displayedBookmarks = _displayedBookmarks.asStateFlow()

    fun setDisplayedBookmarks(bookmarks: List<Bookmark>) {
        _displayedBookmarks.update { bookmarks }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            bookmarkSource.getBookmarks().collect {
                _displayedBookmarks.update { list ->
                    (list + it).distinct().sortedByDescending { it.date_modified }
                }
            }
        }
    }
}
