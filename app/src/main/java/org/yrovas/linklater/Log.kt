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
import org.yrovas.linklater.data.local.PrefDataStore
import org.yrovas.linklater.data.local.Prefs
import org.yrovas.linklater.data.local.asSeverity

val logWriter = platformLogWriter()
val defaultSeverity = if (BuildConfig.DEBUG) {
    Severity.Verbose
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
    config = mutableLoggerConfigInit(logWriter, minSeverity = defaultSeverity), tag = "InitTracing"
)

object EventLog : Logger(
    config = mutableLoggerConfigInit(logWriter, minSeverity = defaultSeverity), tag = "EventTracing"
)

fun Logger.logToSentry(severity: Severity = Severity.Error) {
    this.apply {
        mutableConfig.logWriterList = listOf(logWriter, SentryLogWriter(minSeverity = severity))
    }
}

fun Logger.setSeverity(severity: Severity) {
    this.apply {
        mutableConfig.minSeverity = severity
    }
}

suspend fun setLoggerSeverities(prefStore: PrefDataStore) {
    NetLog.setSeverity(
        prefStore.getPref(Prefs.NET_LOG_SEVERITY, defaultSeverity.ordinal).asSeverity()
    )
    InitLog.setSeverity(
        prefStore.getPref(Prefs.INIT_LOG_SEVERITY, defaultSeverity.ordinal).asSeverity()
    )
    EventLog.setSeverity(
        prefStore.getPref(Prefs.EVENT_LOG_SEVERITY, defaultSeverity.ordinal).asSeverity()
    )
    Log.setSeverity(prefStore.getPref(Prefs.LOG_SEVERITY, defaultSeverity.ordinal).asSeverity())
}

fun logErrorsToSentry() {
    NetLog.logToSentry()
    InitLog.logToSentry()
    EventLog.logToSentry()
    Log.logToSentry()
}

class SentryLogWriter(
    private val messageStringFormatter: MessageStringFormatter = DefaultFormatter,
    private val minSeverity: Severity
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
