package org.yrovas.linklater

import co.touchlab.kermit.DefaultFormatter
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Message
import co.touchlab.kermit.MessageStringFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Tag
import co.touchlab.kermit.mutableLoggerConfigInit
import co.touchlab.kermit.platformLogWriter
import io.sentry.Breadcrumb
import io.sentry.Sentry
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.data.local.PrefDataStore
import org.yrovas.linklater.data.local.Prefs
import org.yrovas.linklater.data.local.asSeverity
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

val logWriter = platformLogWriter()
val defaultSeverity = if (true) {
    Severity.Error
} else {
    Severity.Verbose
}

object Log : Logger(
    config = mutableLoggerConfigInit(logWriter, minSeverity = defaultSeverity), tag = "Debug"
)

object NetLog : Logger(
    config = mutableLoggerConfigInit(logWriter, minSeverity = defaultSeverity),
    tag = "NetworkTracing"
)

object InitLog : Logger(
    config = mutableLoggerConfigInit(logWriter, minSeverity = defaultSeverity),
    tag = "InitTracing"
)

object EventLog : Logger(
    config = mutableLoggerConfigInit(logWriter, minSeverity = defaultSeverity),
    tag = "EventTracing"
)

fun Logger.configure(severity: Severity, sentryLogWriter: SentryLogWriter?) {
    this.apply {
        mutableConfig.minSeverity = severity
        if (sentryLogWriter != null) {
            mutableConfig.logWriterList += arrayOf(sentryLogWriter)
        } else {

        }
    }
}

// TODO how to get preferences & update ktor?
@SingleIn(AppScope::class)
@Inject
data class LogPreferences(
    private val prefStore: PrefDataStore,
    var net: Severity = Severity.Warn,
    var init: Severity = Severity.Warn,
    var event: Severity = Severity.Warn,
    var log: Severity = Severity.Warn,
) {
    private fun configureLoggers() {
//        NetLog.setSeverity(net)
//        InitLog.setSeverity(init)
//        EventLog.setSeverity(event)
//        Log.setSeverityeverity(log)
    }

    private suspend fun retrieveSeverityPrefs() {
        net = prefStore.getPref(Prefs.NET_LOG_SEVERITY, 4).asSeverity()
        init = prefStore.getPref(Prefs.INIT_LOG_SEVERITY, 4).asSeverity()
        event = prefStore.getPref(Prefs.EVENT_LOG_SEVERITY, 4).asSeverity()
        log = prefStore.getPref(Prefs.LOG_SEVERITY, 4).asSeverity()
    }
}

@SingleIn(AppScope::class)
class SentryLogWriter(
    private val messageStringFormatter: MessageStringFormatter = DefaultFormatter,
    private val minSeverity: Severity = Severity.Error
) : LogWriter() {

    override fun isLoggable(tag: String, severity: Severity): Boolean = severity >= minSeverity

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        val formattedLogMessage =
            messageStringFormatter.formatMessage(severity, Tag(tag), Message(message))

        Sentry.addBreadcrumb(Breadcrumb(formattedLogMessage).apply { category = tag })

        if (throwable != null) {
            Sentry.captureException(throwable)
        } else {
            Sentry.captureMessage(formattedLogMessage)
        }
    }
}
