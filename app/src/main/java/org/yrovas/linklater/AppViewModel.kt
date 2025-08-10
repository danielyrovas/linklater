package org.yrovas.linklater

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Severity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.data.local.PrefStore
import org.yrovas.linklater.data.models.LogMessage
import org.yrovas.linklater.data.models.Prefs
import org.yrovas.linklater.data.remote.BookmarkAPI

@AppScope
@Inject
class AppViewModel(private val bookmarkAPI: BookmarkAPI, private val prefStore: PrefStore) :
    ViewModel() {
    init {
        Log.v { "Initialised AppViewModel" }
        viewModelScope.launch {
            initialiseLoggers(prefStore, ViewModelLogWriter(this@AppViewModel, Severity.Debug))
            bookmarkAPI.authenticate(
                endpoint = prefStore.getPref(Prefs.LINKDING_URL, ""),
                token = prefStore.getPref(Prefs.LINKDING_TOKEN, ""),
            )
        }
    }

    suspend fun initialiseLoggers(prefStore: PrefStore, vmLogWriter: ViewModelLogWriter) {
        Log.setSeverity(prefStore.getPref(Prefs.LOG_SEVERITY, defaultSeverity.ordinal).asSeverity())
        NetLog.setSeverity(
            prefStore.getPref(Prefs.NET_LOG_SEVERITY, defaultSeverity.ordinal).asSeverity()
        )
        EventLog.setSeverity(
            prefStore.getPref(Prefs.EVENT_LOG_SEVERITY, defaultSeverity.ordinal).asSeverity()
        )

        Log.setLogWriters(listOf(logWriter, vmLogWriter))
        NetLog.setLogWriters(listOf(logWriter, vmLogWriter))
        EventLog.setLogWriters(listOf(logWriter, vmLogWriter))
    }

    private val _logs = MutableStateFlow(listOf<LogMessage>())
    val logs = _logs.asStateFlow()

    fun sendLog(log: LogMessage) {
        _logs.update { it + log }
    }
}

