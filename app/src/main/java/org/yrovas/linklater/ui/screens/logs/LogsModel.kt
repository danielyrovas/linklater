package org.yrovas.linklater.ui.screens.logs

import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.AppViewModel
import org.yrovas.linklater.data.models.LogMessage
import org.yrovas.linklater.ui.screens.ScreenEffect
import org.yrovas.linklater.ui.screens.ScreenEvent
import org.yrovas.linklater.ui.screens.ScreenModel
import org.yrovas.linklater.ui.screens.logs.LogsModel.Effect
import org.yrovas.linklater.ui.screens.logs.LogsModel.Event

@Inject
class LogsModel(private val appViewModel: () -> AppViewModel) : ScreenModel<Event, Effect>() {
    val logs: StateFlow<List<LogMessage>> = appViewModel().logs

    sealed interface Event : ScreenEvent
    sealed interface Effect : ScreenEffect

    override fun handleEvent(event: Event) {
        TODO("Not yet implemented")
    }
}
