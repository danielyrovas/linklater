package org.yrovas.linklater.ui.screens.saveBookmark

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.onBackPressed
import org.yrovas.linklater.ui.activity.AppActivity
import org.yrovas.linklater.ui.activity.SaveBookmarkActivity
import org.yrovas.linklater.ui.screens.ObserveNavEffects
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkModel.Event

typealias SaveBookmarkActivityScreen = @Composable () -> Unit

@Inject
@Composable
fun SaveBookmarkActivityScreen(saveBookmarkModel: () -> SaveBookmarkModel) {
    val context: Context = LocalContext.current
    val state = viewModel { saveBookmarkModel() }
    val snackState = remember { SnackbarHostState() }
    LaunchedEffect(true) {
        val url = (context as SaveBookmarkActivity).extractURL()
        state.sendEvent(Event.UpdateBookmark(url = url))
    }

    ObserveNavEffects()

    SaveBookmarkScreen(
        snackState = snackState,
        saveBookmarkModel = saveBookmarkModel,
        back = { context.onBackPressed() },
        onSubmitSuccess = suspend {
            Toast.makeText(context, "Saved Bookmark", Toast.LENGTH_SHORT).show()
            (context as AppActivity).finish()
        })
}
