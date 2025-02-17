package org.yrovas.linklater.ui.screens

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.yrovas.linklater.EventLog
import org.yrovas.linklater.InitLog

interface ScreenEvent

interface ScreenEffect

abstract class ScreenModel<Event : ScreenEvent, Effect : ScreenEffect> : ViewModel() {
    private val _event: MutableSharedFlow<Event> = MutableSharedFlow()
    private val event = _event.asSharedFlow()
    private val _effect: Channel<Effect> = Channel()
    private val effect = _effect.receiveAsFlow() // consumeAsFlow?

    init {
        InitLog.d { "Creating ViewModel: ${this.javaClass.simpleName}" }
        subscribeEvents()
    }

    private fun subscribeEvents() {
        viewModelScope.launch {
            event.collect {
                EventLog.v { "Handling event: $it" }
                handleEvent(it)
            }
        }
    }

    // fun sendEvent(builder: () -> Event) = sendEvent(builder())
    fun sendEvent(event: Event) {
        EventLog.d { "Emitting event: $event" }
        viewModelScope.launch { _event.emit(event) }
    }

    // suspend fun sendEventSync(event: Event) {
    //     _event.emit(event)
    // }

    // protected fun sendEffect(builder: () -> Effect) = sendEffect(builder())
    protected fun sendEffect(effect: Effect) {
        EventLog.d { "Emitting effect: $effect" }
        viewModelScope.launch { _effect.send(effect) }
    }

    abstract fun handleEvent(event: Event)

    @SuppressLint("ComposableNaming")
    @Composable
    fun subscribeEffects(onEffect: (Effect) -> Unit) {
        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(effect, lifecycleOwner.lifecycle) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                withContext(Dispatchers.Main.immediate) {
                    effect.collect {
                        EventLog.v { "Received effect: $it" }
                        onEffect(it)
                    }
                }
            }
        }
    }

    @SuppressLint("ComposableNaming")
    @Composable
    fun subscribeEffects(scope: CoroutineScope, onEffect: suspend (Effect) -> Unit) =
        subscribeEffects {
            scope.launch { onEffect(it) }
        }
}
