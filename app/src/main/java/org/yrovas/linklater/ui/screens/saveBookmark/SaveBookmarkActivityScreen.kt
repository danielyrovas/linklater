package org.yrovas.linklater.ui.screens.saveBookmark

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.ui.activity.SaveBookmarkActivity
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkModel.Event

typealias SaveBookmarkActivityScreen = @Composable () -> Unit

@Inject
@Composable
fun SaveBookmarkActivityScreen(saveBookmarkModel: () -> SaveBookmarkModel) {
    val context: Context = LocalContext.current
    val state = viewModel { saveBookmarkModel() }
    LaunchedEffect(true) {
        val url = (context as SaveBookmarkActivity).extractURL()
        state.sendEvent(Event.PasteURL(url))
    }

    SaveBookmarkScreen(
        saveBookmarkModel = saveBookmarkModel,
        back = { (context as Activity).onBackPressed() },
        exitOnSuccess = true
    )
}
