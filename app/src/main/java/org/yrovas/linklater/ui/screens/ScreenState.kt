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

interface ScreenEvent

interface ScreenEffect

abstract class ScreenState<Event : ScreenEvent, Effect : ScreenEffect> : ViewModel() {
    private val _event: MutableSharedFlow<Event> = MutableSharedFlow()
    private val event = _event.asSharedFlow()
    private val _effect: Channel<Effect> = Channel()
    private val effect = _effect.receiveAsFlow() // consumeAsFlow?

    private fun subscribeEvents() {
        viewModelScope.launch {
            event.collect {
                handleEvent(it)
            }
        }
    }

    //    fun sendEvent(builder: () -> Event) = sendEvent(builder())
    fun sendEvent(event: Event) {
        viewModelScope.launch { _event.emit(event) }
    }

    //    protected fun sendEffect(builder: () -> Effect) = sendEffect(builder())
    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch { _effect.send(effect) }
    }

    abstract fun handleEvent(event: Event)

    init {
        subscribeEvents()
    }

    @SuppressLint("ComposableNaming")
    @Composable
    fun subscribeEffects(onEffect: (Effect) -> Unit) {
        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(effect, lifecycleOwner.lifecycle) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                withContext(Dispatchers.Main.immediate) {
                    effect.collect(onEffect)
                }
            }
        }
    }

    @SuppressLint("ComposableNaming")
    @Composable
    fun subscribeEffects(
        scope: CoroutineScope,
        onEffect: suspend (Effect) -> Unit,
    ) {
        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(effect, lifecycleOwner.lifecycle) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                withContext(Dispatchers.Main.immediate) {
                    effect.collect {
                        scope.launch { onEffect(it) }
                    }
                }
            }
        }
    }
}
