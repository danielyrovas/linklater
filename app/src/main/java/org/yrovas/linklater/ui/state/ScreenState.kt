package org.yrovas.linklater.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

interface ScreenEvent

interface ScreenEffect

abstract class ScreenState<Event : ScreenEvent, Effect : ScreenEffect> :
    ViewModel() {
    private val _event: MutableSharedFlow<Event> = MutableSharedFlow()
    protected val event = _event.asSharedFlow()
    private val _effect: Channel<Effect> = Channel()
    val effect = _effect.receiveAsFlow() // consumeAsFlow?

    private fun subscribeEvents() {
        viewModelScope.launch {
            event.collect {
                handleEvent(it)
            }
        }
    }

    fun sendEvent(builder: () -> Event) = sendEvent(builder())
    fun sendEvent(event: Event) {
        viewModelScope.launch { _event.emit(event) }
    }

    protected fun sendEffect(builder: () -> Effect) = sendEffect(builder())
    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch { _effect.send(effect) }
    }

    abstract fun handleEvent(event: Event)

    init {
        subscribeEvents()
    }
}
