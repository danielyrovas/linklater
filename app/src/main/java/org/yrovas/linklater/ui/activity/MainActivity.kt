package org.yrovas.linklater

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.spec.NavGraphSpec
import kotlinx.coroutines.launch
import org.yrovas.linklater.ui.screens.NavGraphs
import org.yrovas.linklater.ui.theme.AppTheme

abstract class AppActivity() : ComponentActivity() {
    private val appViewModel: AppViewModelImpl by viewModels()

    fun launch(job: suspend () -> Unit) {
        lifecycleScope.launch { job() }
    }

    fun setContent(navGraph: NavGraphSpec) {
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        appViewModel.setup(this)
        setContent {
            val snackState = remember { SnackbarHostState() }
            AppTheme {
                DestinationsNavHost(
                    navGraph = navGraph,
                    dependenciesContainerBuilder = {
                        dependency(navGraph) { appViewModel }
                        dependency(navGraph) { snackState }
                    })
            }
        }
    }
}

class MainActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(NavGraphs.root)
    }
}
