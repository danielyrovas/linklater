package org.yrovas.linklater.ui.screens

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.AppViewModel
import org.yrovas.linklater.Log
import org.yrovas.linklater.ui.screens.home.HomeScreen
import org.yrovas.linklater.ui.screens.logs.LogsScreen
import org.yrovas.linklater.ui.screens.preferences.PreferencesScreen
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkActivityScreen
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkScreen

sealed interface Direction {
    data object Down : Direction
    data object Up : Direction
}

val LocalBackStack: ProvidableCompositionLocal<NavBackStack> =
    compositionLocalOf { NavBackStack() }

val LocalSnackState: ProvidableCompositionLocal<SnackbarHostState> =
    compositionLocalOf { SnackbarHostState() }

typealias NavigationHost = @Composable (Destination) -> Unit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Inject
fun NavigationHost(
    @Assisted entryDestination: Destination,
    appViewModel: () -> AppViewModel,
    homeScreen: () -> HomeScreen,
    preferencesScreen: () -> PreferencesScreen,
    saveBookmarkScreen: () -> SaveBookmarkScreen,
    saveBookmarkActivityScreen: () -> SaveBookmarkActivityScreen,
    logsScreen: () -> LogsScreen,
) {
    val backStack = rememberNavBackStack(entryDestination)
    val snackState = remember { SnackbarHostState() }
    Log.v { "Composed with entry destination: $entryDestination" }
    viewModel { appViewModel() }

    val animateEntry = { dir: Direction ->
        NavDisplay.transitionSpec {
            slideInVertically(
                initialOffsetY = { if (dir is Direction.Down) -it else it },
            ) togetherWith ExitTransition.KeepUntilTransitionsFinished
        } + NavDisplay.popTransitionSpec {
            EnterTransition.None togetherWith
                slideOutVertically(
                    targetOffsetY = { if (dir is Direction.Down) -it else it },
                )
        } + NavDisplay.predictivePopTransitionSpec {
            EnterTransition.None togetherWith
                slideOutVertically(
                    targetOffsetY = { if (dir is Direction.Down) -it else it },
                )
        }
    }

    CompositionLocalProvider(LocalBackStack provides backStack, LocalSnackState provides snackState) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSavedStateNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
                rememberSceneSetupNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<Destination.Home> {
                    homeScreen()()
                }
                entry<Destination.Logs> {
                    logsScreen()()
                }
                entry<Destination.Preferences>(
                    metadata = animateEntry(Direction.Down)
                ) {
                    preferencesScreen()()
                }
                entry<Destination.SaveBookmarkActivity>(
                    metadata = animateEntry(Direction.Up)
                ) {
                    saveBookmarkActivityScreen()()
                }
                entry<Destination.SaveBookmark>(
                    metadata = animateEntry(Direction.Up)
                ) {
                    saveBookmarkScreen()()
                }
            },

            transitionSpec = {
                slideInHorizontally(initialOffsetX = { it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { -it })
            },
            popTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
            },
            predictivePopTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
            }
        )
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
//        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            SnackbarHost(hostState = snackState)
//        }
    }
}
