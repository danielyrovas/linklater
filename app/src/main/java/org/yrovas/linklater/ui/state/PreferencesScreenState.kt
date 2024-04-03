package org.yrovas.linklater.ui.state

import android.content.Context
import androidx.lifecycle.ViewModel
import org.yrovas.linklater.AppViewModel

class PreferencesScreenState(
    private val appViewModel: AppViewModel,
) : ViewModel() {

    val bookmarkURL = appViewModel.bookmarkURL
    fun saveBookmarkURL(context: Context, url: String) {
        appViewModel.saveBookmarkURL(context, url)
    }

    fun checkBookmarkAPIToken(token: String) = token.length > 10
    val bookmarkAPIToken = appViewModel.bookmarkAPIToken
    fun saveBookmarkAPIToken(context: Context, token: String) {
        appViewModel.saveBookmarkAPIToken(context, token)
    }
}
