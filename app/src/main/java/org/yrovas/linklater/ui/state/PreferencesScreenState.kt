package org.yrovas.linklater.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.data.local.PrefDataStore
import org.yrovas.linklater.data.local.Prefs
import org.yrovas.linklater.data.remote.BookmarkAPI

//@Provides
//fun providePreferencesScreenState(appViewModel: () -> AppViewModelImpl): PreferencesScreenState =
//    PreferencesScreenState(appViewModel)

@Inject
class PreferencesScreenState(
    private val bookmarkAPI: BookmarkAPI,
    private val prefStore: PrefDataStore,
) : ViewModel() {
    init {
        viewModelScope.launch(Dispatchers.IO) {
            saveBookmarkAPIToken(prefStore.getPref(Prefs.LINKDING_TOKEN, ""))
            saveBookmarkURL(prefStore.getPref(Prefs.LINKDING_URL, ""))
        }
    }
    private val _bookmarkEndpoint: MutableStateFlow<String> = MutableStateFlow("")
    var bookmarkEndpoint = _bookmarkEndpoint.asStateFlow()
    private val _bookmarkAPIToken: MutableStateFlow<String> = MutableStateFlow("")
    var bookmarkAPIToken = _bookmarkAPIToken.asStateFlow()

    private fun saveBookmarkConf(
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

    fun saveBookmarkURL(url: String) {
        saveBookmarkConf(url = url)
    }

    fun saveBookmarkAPIToken(token: String) {
        saveBookmarkConf(token = token)
    }
}
