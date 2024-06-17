package org.yrovas.linklater.ui.component

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.ui.screens.HomeScreen
import org.yrovas.linklater.ui.screens.NavEntryScreen
import org.yrovas.linklater.ui.screens.PreferencesScreen
import org.yrovas.linklater.ui.screens.SaveBookmarkActivityScreen
import org.yrovas.linklater.ui.screens.SaveBookmarkScreen
import org.yrovas.linklater.ui.state.HomeScreenState
import org.yrovas.linklater.ui.state.PreferencesScreenState
import org.yrovas.linklater.ui.state.SaveBookmarkScreenState

typealias AppHost = @Composable (NavEntryScreen, SnackbarHostState) -> Unit

@Inject
@Composable
fun AppHost(
    homeScreenState: () -> HomeScreenState,
    preferencesScreenState: () -> PreferencesScreenState,
    saveBookmarkScreenState: () -> SaveBookmarkScreenState,
    @Assisted entryScreen: NavEntryScreen,
    @Assisted snackState: SnackbarHostState,
) {
    val nav = rememberNavController()
    NavHost(nav, startDestination = entryScreen) {
        composable<HomeScreen> {
            HomeScreen(nav, snackState, homeScreenState)
        }
        composable<SaveBookmarkActivityScreen> {
            SaveBookmarkActivityScreen(nav, snackState, saveBookmarkScreenState)
        }
        composable<SaveBookmarkScreen> {
            SaveBookmarkScreen(nav, snackState, saveBookmarkScreenState)
        }
        composable<PreferencesScreen> {
            PreferencesScreen(nav, snackState, preferencesScreenState)
        }
    }
}
