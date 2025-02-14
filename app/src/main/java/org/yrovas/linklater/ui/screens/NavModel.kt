package org.yrovas.linklater.ui.screens

import androidx.compose.runtime.Composable


object NavModel : ScreenModel<NavModel.Event, NavModel.Effect>() {
    sealed interface Event : ScreenEvent
    sealed interface Effect : ScreenEffect {
        data class NavigateTo(val destination: Destination) : Effect
        data object NavigateUpOrExit : Effect
        data object NavigateUp : Effect
    }

    override fun handleEvent(event: Event) { }

    fun navigate(destination: Destination) {
        sendEffect(Effect.NavigateTo(destination))
    }

    fun navigateUp() {
        sendEffect(Effect.NavigateUp)
    }
    fun navigateUpOrExit() {
        sendEffect(Effect.NavigateUpOrExit)
    }
}

@Composable
fun ObserveNavEffects() {
    val nav = LocalNavController.current!!
    NavModel.subscribeEffects { effect ->
        when (effect) {
            NavModel.Effect.NavigateUp -> nav.popBackStack()
            NavModel.Effect.NavigateUpOrExit -> nav.navigateUp()
            is NavModel.Effect.NavigateTo -> nav.navigate(effect.destination)
        }
    }
}
