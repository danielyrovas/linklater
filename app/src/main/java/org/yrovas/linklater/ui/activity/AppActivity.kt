package org.yrovas.linklater.ui.activity

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navGraph
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.NavHostGraphSpec
import kotlinx.coroutines.launch
import org.yrovas.linklater.AppViewModelImpl
import org.yrovas.linklater.ui.state.*
import org.yrovas.linklater.ui.theme.AppTheme

abstract class AppActivity : ComponentActivity() {
    private val appViewModel: AppViewModelImpl by viewModels()

    fun launch(job: suspend () -> Unit) {
        lifecycleScope.launch { job() }
    }

    fun setContent(navGraph: NavHostGraphSpec) {
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        appViewModel.setup(this)
        setContent {
            val snackState = remember { SnackbarHostState() }
            AppTheme {
                DestinationsNavHost(
                    navGraph = navGraph,
                    dependenciesContainerBuilder = {
                        navGraph(navGraph) {
                            dependency(appViewModel)
                            dependency(snackState)
                            dependency(viewModel<SaveBookmarkScreenState>())
                            dependency(viewModel<HomeScreenState>())
                            dependency(viewModel<PreferencesScreenState>())
                        }
                    })
            }
        }
    }
}
