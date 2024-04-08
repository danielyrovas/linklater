package org.yrovas.linklater.ui.state

import androidx.lifecycle.ViewModel
import org.yrovas.linklater.AppViewModel

class PreferencesScreenState(
    private val appViewModel: AppViewModel,
) : ViewModel() {
    val bookmarkURL = appViewModel.bookmarkEndpoint
    fun saveBookmarkURL(url: String) {
        appViewModel.saveBookmarkConf(url = url)
    }

    val bookmarkAPIToken = appViewModel.bookmarkAPIToken
    fun saveBookmarkAPIToken(token: String) {
        appViewModel.saveBookmarkConf(token = token)
    }
}
