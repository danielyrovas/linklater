package org.yrovas.linklater.ui.screens

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.ui.screens.home.HomeScreen
import org.yrovas.linklater.ui.screens.preferences.PreferencesScreen
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkActivityScreen
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkScreen

typealias NavigationHost = @Composable (Destination) -> Unit

@Inject
@Composable
fun NavigationHost(
    nav: NavHostController,
    @Assisted entryDestination: Destination
) {
//    val nav = rememberNavController()
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
        composable<Destination.Home> { HomeScreen() }
        if (entryDestination == Destination.SaveBookmarkActivity) {
            composable<Destination.SaveBookmarkActivity> { SaveBookmarkActivityScreen() }
        } else {
            composable<Destination.SaveBookmark> { SaveBookmarkScreen() }
        }
        composable<Destination.Preferences> { PreferencesScreen() }
    }
}
