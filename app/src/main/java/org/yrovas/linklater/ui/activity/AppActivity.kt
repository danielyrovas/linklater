package org.yrovas.linklater.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.yrovas.linklater.AppComponent
import org.yrovas.linklater.create
import org.yrovas.linklater.data.local.Prefs
import org.yrovas.linklater.ui.screens.EntryDestination
import org.yrovas.linklater.ui.theme.AppTheme

fun Context.launch(
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    job: suspend () -> Unit,
) {
    (this as AppActivity).launch(dispatcher, job)
}

abstract class AppActivity : ComponentActivity() {

    abstract val entryScreen: EntryDestination

    private val component by lazy(LazyThreadSafetyMode.NONE) {
        AppComponent::class.create(this)
    }

    private val snackState: SnackbarHostState by lazy { SnackbarHostState() }

    fun launch(dispatcher: CoroutineDispatcher = Dispatchers.Main, job: suspend () -> Unit) {
        lifecycleScope.launch(dispatcher) { job() }
    }

    fun showSnackbar(message: String) {
        launch { snackState.showSnackbar(message) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        enableEdgeToEdge()

        val navHost = component.navigationHost
        launch {
            // setup code that runs on first boot
            component.bookmarkAPI.authenticate(
                endpoint = component.prefStore.getPref(Prefs.LINKDING_URL, ""),
                token = component.prefStore.getPref(Prefs.LINKDING_TOKEN, ""),
            )
        }
        setContent {
            AppTheme {
                navHost(entryScreen, snackState)
            }
        }
    }
}
