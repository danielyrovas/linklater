package org.yrovas.linklater

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.mutableLoggerConfigInit
import co.touchlab.kermit.platformLogWriter
import org.yrovas.linklater.data.local.PrefStore
import org.yrovas.linklater.data.models.LogMessage
import org.yrovas.linklater.data.models.Prefs


val logWriter = platformLogWriter()

val defaultSeverity = if (BuildConfig.DEBUG) {
    Severity.Verbose
} else {
    Severity.Warn
}

object Log : Logger(
    config = mutableLoggerConfigInit(logWriter, minSeverity = defaultSeverity),
    tag = "DebugTracing"
)

object NetLog : Logger(
    config = mutableLoggerConfigInit(logWriter, minSeverity = defaultSeverity),
    tag = "NetworkTracing"
)

object EventLog : Logger(
    config = mutableLoggerConfigInit(logWriter, minSeverity = defaultSeverity),
    tag = "EventTracing"
)

fun Logger.setSeverity(severity: Severity) {
    this.apply {
        mutableConfig.minSeverity = severity
    }
}

fun Logger.setLogWriters(logWriters: List<LogWriter>) {
    this.apply {
        mutableConfig.logWriterList = logWriters
    }
}

internal fun Int.asSeverity(): Severity {
    return when (this) {
        in 0..5 -> Severity.entries[this]
        else -> throw Throwable("value is not a severity value: $this")
    }
}

class ViewModelLogWriter(private val vm: AppViewModel, val minSeverity: Severity) : LogWriter() {
    override fun isLoggable(tag: String, severity: Severity): Boolean = severity >= minSeverity
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        vm.sendLog(LogMessage(severity = severity, message = message, throwable = throwable))
    }
}

//class SentryLogWriter(
//    private val messageStringFormatter: MessageStringFormatter = DefaultFormatter,
//    private val minSeverity: Severity
//) : LogWriter() {
//
//    override fun isLoggable(tag: String, severity: Severity): Boolean = severity >= minSeverity
//
//    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
//        val formattedLogMessage =
//            messageStringFormatter.formatMessage(severity, Tag(tag), Message(message))
//
//        Sentry.addBreadcrumb(Breadcrumb(formattedLogMessage).apply { category = tag })
//
//        if (throwable != null) {
//            Sentry.captureException(throwable)
//        } else {
//            Sentry.captureMessage(formattedLogMessage)
//        }
//    }
//}
