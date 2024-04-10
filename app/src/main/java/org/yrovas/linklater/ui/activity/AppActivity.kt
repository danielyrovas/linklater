package org.yrovas.linklater.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.PreferencesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SaveBookmarkScreenDestination
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.destination
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavHostGraphSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.AppComponent
import org.yrovas.linklater.create
import org.yrovas.linklater.data.local.Prefs
import org.yrovas.linklater.ui.state.HomeScreenState
import org.yrovas.linklater.ui.state.PreferencesScreenState
import org.yrovas.linklater.ui.state.SaveBookmarkScreenState
import org.yrovas.linklater.ui.theme.AppTheme

abstract class AppActivity : ComponentActivity() {
    val component by lazy(LazyThreadSafetyMode.NONE) {
        AppComponent::class.create(this)
    }
    private val _setup_complete: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    private var setup_complete = _setup_complete.asStateFlow()

    fun launch(job: suspend () -> Unit) {
        lifecycleScope.launch { job() }
    }

    protected fun setContent(navGraph: NavHostGraphSpec) {
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        val destinationHost = component.destinationHost
        launch {
            // setup code that runs on first boot
            component.bookmarkAPI.authenticate(
                endpoint = component.prefStore.getPref(Prefs.LINKDING_URL, ""),
                token = component.prefStore.getPref(Prefs.LINKDING_TOKEN, ""),
            )
            delay(3000)
            _setup_complete.update { true }
        }
        setContent {
            val snackState = remember { SnackbarHostState() }
            AppTheme {
                destinationHost(navGraph, snackState, setup_complete)
            }
        }
    }
}

typealias DestinationHost = @Composable (NavHostGraphSpec, SnackbarHostState, StateFlow<Boolean>) -> Unit

@Inject
@Composable
fun DestinationHost(
    homeScreenState: () -> HomeScreenState,
    preferencesScreenState: () -> PreferencesScreenState,
    saveBookmarkScreenState: () -> SaveBookmarkScreenState,
    navGraph: NavHostGraphSpec,
    snackbarHostState: SnackbarHostState,
    setup_complete: StateFlow<Boolean>,
) {
    DestinationsNavHost(navGraph = navGraph, dependenciesContainerBuilder = {
        dependency(snackbarHostState)
        destination(HomeScreenDestination) {
            dependency(setup_complete)
        }
        provideState(HomeScreenDestination, homeScreenState)
        provideState(SaveBookmarkScreenDestination, saveBookmarkScreenState)
        provideState(PreferencesScreenDestination, preferencesScreenState)
    })
}

@SuppressLint("ComposableNaming")
@Composable
private fun <T> DependenciesContainerBuilder<T>.provideState(
    destination: DestinationSpec,
    state: () -> ViewModel,
) {
    destination(destination) { dependency(state) }
}
