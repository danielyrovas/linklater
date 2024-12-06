package org.yrovas.linklater.data.models

import co.touchlab.kermit.Severity

data class LogMessage(
    val message: String,
    val severity: Severity,
    val throwable: Throwable? = null,
)
