package org.yrovas.linklater.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavGraphBuilder
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

val LocalNavController: ProvidableCompositionLocal<NavHostController?> = staticCompositionLocalOf { null }

@Inject
@Composable
fun NavigationHost(
    homeScreen: () -> HomeScreen,
    preferencesScreen: () -> PreferencesScreen,
    saveBookmarkScreen: () -> SaveBookmarkScreen,
    saveBookmarkActivityScreen: () -> SaveBookmarkActivityScreen,
    @Assisted entryDestination: Destination
) {
    val nav = rememberNavController()
    CompositionLocalProvider(LocalNavController provides nav) {
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
            route<Destination.Home>(homeScreen)
            route<Destination.Preferences>(preferencesScreen)
            if (entryDestination == Destination.SaveBookmarkActivity) {
                route<Destination.SaveBookmarkActivity>(saveBookmarkActivityScreen)
            } else {
                route<Destination.SaveBookmark>(saveBookmarkScreen)
            }
        }
    }
}

inline fun <reified T : Any> NavGraphBuilder.route(crossinline content: () -> @Composable () -> Unit) {
    composable<T> { content()() }
}
