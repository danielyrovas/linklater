package org.yrovas.linklater.ui.screens

import androidx.compose.runtime.Composable


object NavModel : ScreenModel<NavModel.Event, NavModel.Effect>() {
    sealed interface Event : ScreenEvent
    sealed interface Effect : ScreenEffect {
        data class NavigateTo(val destination: Destination) : Effect
        data object NavigateBackOrExit : Effect
        data object NavigateBack : Effect
    }

    override fun handleEvent(event: Event) { }

    fun navigate(destination: Destination) {
        sendEffect(Effect.NavigateTo(destination))
    }

    fun navigateBack() {
        sendEffect(Effect.NavigateBack)
    }
    fun navigateBackOrExit() {
        sendEffect(Effect.NavigateBackOrExit)
    }
}

@Composable
fun ObserveNavEffects() {
    val nav = LocalNavController.current!!
    NavModel.subscribeEffects { effect ->
        when (effect) {
            NavModel.Effect.NavigateBack -> nav.popBackStack()
            NavModel.Effect.NavigateBackOrExit -> nav.navigateUp()
            is NavModel.Effect.NavigateTo -> nav.navigate(effect.destination)
        }
    }
}
