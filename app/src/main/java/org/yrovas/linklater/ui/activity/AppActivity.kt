package org.yrovas.linklater.ui.activity

import android.content.Intent
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import com.ramcosta.composedestinations.spec.NavHostGraphSpec
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.launch
import org.yrovas.linklater.AppComponent
import org.yrovas.linklater.create
import org.yrovas.linklater.data.local.Prefs
import org.yrovas.linklater.ui.theme.AppTheme

abstract class AppActivity : ComponentActivity() {
    private val component by lazy(LazyThreadSafetyMode.NONE) {
        Log.d("DEBUG/create", "AppComponent: CREATE")
        AppComponent::class.create(this)
    }

    fun launch(dispatcher: CoroutineDispatcher = Dispatchers.Main, job: suspend () -> Unit) {
        lifecycleScope.launch(dispatcher) { job() }
    }

    protected fun setContent(navGraph: NavHostGraphSpec) {
        StrictMode.setVmPolicy(
            VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .build()
        )

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        val destinationHost = component.destinationHost
        launch {
            // setup code that runs on first boot
            component.bookmarkAPI.authenticate(
                endpoint = component.prefStore.getPref(Prefs.LINKDING_URL, ""),
                token = component.prefStore.getPref(Prefs.LINKDING_TOKEN, ""),
            )
        }
        setContent {
            val snackState = remember { SnackbarHostState() }
            AppTheme {
                destinationHost(navGraph, snackState)
            }
        }
    }
}
