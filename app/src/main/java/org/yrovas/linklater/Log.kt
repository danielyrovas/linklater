package org.yrovas.linklater

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.mutableLoggerConfigInit
import co.touchlab.kermit.platformLogWriter
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.data.local.PrefDataStore
import org.yrovas.linklater.data.local.Prefs
import org.yrovas.linklater.data.local.asSeverity
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

val logWriters = arrayOf(platformLogWriter())
val defaultSeverity = Severity.Verbose

object Log : Logger(
    config = mutableLoggerConfigInit(*logWriters, minSeverity = defaultSeverity),
    tag = "Debug"
)

object NetLog : Logger(
    config = mutableLoggerConfigInit(*logWriters, minSeverity = defaultSeverity),
    tag = "NetworkTracing"
)

object InitLog : Logger(
    config = mutableLoggerConfigInit(*logWriters, minSeverity = defaultSeverity),
    tag = "InitTracing"
)

object EventLog : Logger(
    config = mutableLoggerConfigInit(*logWriters, minSeverity = defaultSeverity),
    tag = "EventTracing"
)

fun Logger.setSeverity(severity: Severity) {
    this.mutableConfig.minSeverity = severity
}

// TODO how to get preferences & update ktor?
@SingleIn(AppScope::class)
@Inject
data class SeverityPreferences(
    private val prefStore: PrefDataStore,
    var net: Severity = Severity.Warn,
    var init: Severity = Severity.Warn,
    var event: Severity = Severity.Warn,
    var log: Severity = Severity.Warn,
) {
    fun configureLoggers() {
        NetLog.setSeverity(net)
        InitLog.setSeverity(init)
        EventLog.setSeverity(event)
        Log.setSeverity(log)
    }

    suspend fun retrieveSeverityPrefs() {
        net = prefStore.getPref(Prefs.NET_LOG_SEVERITY, 4).asSeverity()
        init = prefStore.getPref(Prefs.INIT_LOG_SEVERITY, 4).asSeverity()
        event = prefStore.getPref(Prefs.EVENT_LOG_SEVERITY, 4).asSeverity()
        log = prefStore.getPref(Prefs.LOG_SEVERITY, 4).asSeverity()
    }
}
