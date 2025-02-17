package org.yrovas.linklater.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.yrovas.linklater.AppComponent
import org.yrovas.linklater.create
import org.yrovas.linklater.data.local.Prefs
import org.yrovas.linklater.ui.screens.Destination
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.Log

fun Context.launch(
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    job: suspend () -> Unit,
) {
    (this as AppActivity).launch(dispatcher, job)
}

abstract class AppActivity : ComponentActivity() {

    abstract val entryScreen: Destination

    private val appComponent by lazy(LazyThreadSafetyMode.NONE) {
        AppComponent::class.create(this)
    }

    fun launch(dispatcher: CoroutineDispatcher = Dispatchers.Main, job: suspend () -> Unit) {
        lifecycleScope.launch(dispatcher) { job() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        enableEdgeToEdge()

        val navHost = appComponent.navigationHost
        launch {
            appComponent.bookmarkAPI.authenticate(
                endpoint = appComponent.prefStore.getPref(Prefs.LINKDING_URL, ""),
                token = appComponent.prefStore.getPref(Prefs.LINKDING_TOKEN, ""),
            )
        }
        Log.v { "Initialised Android App" }
        setContent {
            AppTheme { navHost(entryScreen) }
        }
    }
}
