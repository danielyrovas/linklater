package org.yrovas.linklater

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.data.EmptyPrefStore
import org.yrovas.linklater.data.PrefDataStore
import org.yrovas.linklater.data.Prefs
import org.yrovas.linklater.domain.BookmarkAPI
import org.yrovas.linklater.domain.EmptyBookmarkAPI

abstract class AppViewModel(
    val bookmarkAPI: BookmarkAPI,
    private val prefStore: PrefDataStore,
) : ViewModel() {

    private val _bookmarkEndpoint: MutableStateFlow<String> = MutableStateFlow("")
    var bookmarkEndpoint = _bookmarkEndpoint.asStateFlow()
    private val _bookmarkAPIToken: MutableStateFlow<String> = MutableStateFlow("")
    var bookmarkAPIToken = _bookmarkAPIToken.asStateFlow()

    open fun saveBookmarkConf(
        url: String? = null,
        token: String? = null,
    ) {
        if (url != null) _bookmarkEndpoint.update { url }
        if (token != null) _bookmarkAPIToken.update { token }

        viewModelScope.launch(Dispatchers.IO) {
            bookmarkAPI.authenticate(
                bookmarkEndpoint.value, bookmarkAPIToken.value
            )
            prefStore.setPref(Prefs.LINKDING_URL, bookmarkEndpoint.value)
            prefStore.setPref(Prefs.LINKDING_TOKEN, bookmarkAPIToken.value)
        }
    }

    fun loadPrefs() {
        viewModelScope.launch(Dispatchers.IO) {
            _bookmarkEndpoint.update {
                prefStore.getPref(Prefs.LINKDING_URL, "")
            }
            _bookmarkAPIToken.update {
                prefStore.getPref(Prefs.LINKDING_TOKEN, "")
            }
            bookmarkAPI.authenticate(
                bookmarkEndpoint.value, bookmarkAPIToken.value
            )
        }
    }
}

class AppViewModelImpl(
    bookmarkAPI: BookmarkAPI, prefStore: PrefDataStore,
) : AppViewModel(bookmarkAPI, prefStore)

class PreviewAppViewModel(
    bookmarkAPI: BookmarkAPI = EmptyBookmarkAPI(),
    prefStore: PrefDataStore = EmptyPrefStore(),
) : AppViewModel(bookmarkAPI, prefStore)
