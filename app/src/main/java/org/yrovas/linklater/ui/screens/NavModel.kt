package org.yrovas.linklater.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn


//class NavModel : ScreenModel<NavModel.Event, Destination>() {
//    sealed interface Event : ScreenEvent {
//        data class NavigateTo(val destination: Destination) : Event
//    }
//
//    override fun handleEvent(event: Event) {
//        when (event) {
//            is Event.NavigateTo -> sendEffect(event.destination)
//        }
//    }
//
//    fun navigate(destination: Destination) {
//        sendEffect(destination)
//    }
//}

@SingleIn(AppScope::class)
object NavModel {
    private val _destination: MutableSharedFlow<Destination> = MutableSharedFlow()
    val destination = _destination.asSharedFlow()

    fun tryNavigate(destination: Destination) {
        _destination.tryEmit(destination)
    }

    suspend fun navigate(destination: Destination) {
        _destination.emit(destination)
    }
}


typealias NavEffects = @Composable () -> Unit

@Inject
@Composable
fun NavEffects(nav: NavHostController) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                NavModel.destination.collect { nav.navigate(it) }
            }
        }
    }
}
