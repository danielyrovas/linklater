package org.yrovas.linklater.ui.screens.saveBookmark

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import org.yrovas.linklater.onBackPressed
import org.yrovas.linklater.ui.activity.AppActivity
import org.yrovas.linklater.ui.activity.SaveBookmarkActivity
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkState.Event

@Composable
fun SaveBookmarkActivityScreen(
    nav: NavController,
    snackState: SnackbarHostState,
    saveBookmarkState: () -> SaveBookmarkState,
) {
    val context: Context = LocalContext.current
    val state = viewModel { saveBookmarkState() }
    LaunchedEffect(true) {
        val url = (context as SaveBookmarkActivity).extractURL()
        Log.d("DEBUG/nav", "Extracting Intent URL: $url")
        state.sendEvent(Event.UpdateBookmark(url = url))
    }

    SaveBookmarkScreen(nav = nav,
        snackState = snackState,
        state = saveBookmarkState,
        back = { context.onBackPressed() },
        onSubmitSuccess = suspend {
            Toast.makeText(context, "Saved Bookmark", Toast.LENGTH_SHORT).show()
            (context as AppActivity).finish()
        })
}
