package org.yrovas.linklater.ui.component

import android.annotation.SuppressLint
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.PreferencesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SaveBookmarkActivityScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SaveBookmarkScreenDestination
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.destination
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavHostGraphSpec
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.ui.state.HomeScreenState
import org.yrovas.linklater.ui.state.PreferencesScreenState
import org.yrovas.linklater.ui.state.SaveBookmarkScreenState

typealias DestinationHost = @Composable (NavHostGraphSpec, SnackbarHostState) -> Unit

@Inject
@Composable
fun DestinationHost(
    homeScreenState: () -> HomeScreenState,
    preferencesScreenState: () -> PreferencesScreenState,
    saveBookmarkScreenState: () -> SaveBookmarkScreenState,
    @Assisted navGraph: NavHostGraphSpec,
    @Assisted snackbarHostState: SnackbarHostState,
) {
    DestinationsNavHost(navGraph = navGraph, dependenciesContainerBuilder = {
        dependency(snackbarHostState)
        provideState(HomeScreenDestination, homeScreenState)
        provideState(SaveBookmarkScreenDestination, saveBookmarkScreenState)
        provideState(
            SaveBookmarkActivityScreenDestination, saveBookmarkScreenState
        )
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
