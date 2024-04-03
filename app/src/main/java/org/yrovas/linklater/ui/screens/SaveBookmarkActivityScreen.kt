package org.yrovas.linklater.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import org.yrovas.linklater.AppViewModel
import org.yrovas.linklater.onBackPressed
import org.yrovas.linklater.ui.activity.*
import org.yrovas.linklater.ui.state.SaveBookmarkScreenState

@Destination<SaveBookmarkActivityGraph>(start = true)
@Composable
fun SaveBookmarkActivityScreen(
    nav: DestinationsNavigator,
    appViewModel: AppViewModel,
    state: SaveBookmarkScreenState,
    snackState: SnackbarHostState,
    context: Context = LocalContext.current,
) {
    LaunchedEffect(true) {
        val url = (context as SaveBookmarkActivity).extractURL()
        Log.d("DEBUG/nav", "Extracting Intent URL: $url")
        state.updateBookmark(url = url)
    }
    SaveBookmarkScreen(nav = nav,
        appViewModel = appViewModel,
        snackState = snackState,
        state = state,
        back = { context.onBackPressed() },
        onSubmitSuccess = suspend {
            delay(1000)
            (context as AppActivity).finish()
        })
}
