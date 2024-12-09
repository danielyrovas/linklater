package org.yrovas.linklater.ui.screens

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.ui.screens.home.HomeDestination
import org.yrovas.linklater.ui.screens.home.HomeScreen
import org.yrovas.linklater.ui.screens.home.HomeState
import org.yrovas.linklater.ui.screens.preferences.PreferencesDestination
import org.yrovas.linklater.ui.screens.preferences.PreferencesScreen
import org.yrovas.linklater.ui.screens.preferences.PreferencesState
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkActivityDestination
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkActivityScreen
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkDestination
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkScreen
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkState

typealias NavigationHost = @Composable (EntryDestination, SnackbarHostState) -> Unit

// TODO: remove state injection here, and add @assisted to compose screens. Add typealias for
// screen and then inject directly into the compose function
@Inject
@Composable
fun NavigationHost(
    homeState: () -> HomeState,
    preferencesState: () -> PreferencesState,
    saveBookmarkState: () -> SaveBookmarkState,
    @Assisted entryDestination: EntryDestination,
    @Assisted snackState: SnackbarHostState,
) {
    val nav = rememberNavController()
    NavHost(nav, startDestination = entryDestination, enterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500)
        )
    }, exitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500)
        )
    }, popEnterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500)
        )
    }, popExitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500)
        )
    }) {
        composable<HomeDestination> {
            HomeScreen(nav, snackState, homeState)
        }
        if (entryDestination == SaveBookmarkActivityDestination) {
            composable<SaveBookmarkActivityDestination> {
                SaveBookmarkActivityScreen(nav, snackState, saveBookmarkState)
            }
        } else {
            composable<SaveBookmarkDestination> {
                SaveBookmarkScreen(nav, snackState, saveBookmarkState)
            }
        }
        composable<PreferencesDestination> {
            PreferencesScreen(nav, snackState, preferencesState)
        }
    }
}
