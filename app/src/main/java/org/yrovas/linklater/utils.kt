package org.yrovas.linklater

import android.content.*
import android.net.Uri
import android.util.Log
import kotlinx.datetime.Instant
import java.net.MalformedURLException
import java.net.URL
import kotlin.math.abs

fun checkURL(url: String) = url.contains(Regex("^https?://.+[.].+"))

fun timeAgo(timestamp: Instant, now: Instant): String {
    val differenceInSeconds = abs((now - timestamp).inWholeSeconds)
    return when {
        differenceInSeconds < 60 -> "just now"
        differenceInSeconds < 3600 -> "${(differenceInSeconds / 60).toInt()}m ago"
        differenceInSeconds < 86400 -> "${(differenceInSeconds / 3600).toInt()}h ago"
        differenceInSeconds < 2628000 -> {
            val days = (differenceInSeconds / 86400).toInt()
            "$days ${if (days == 1) "day" else "days"} ago"
        }
        differenceInSeconds < 31536000 -> {
            val months = (differenceInSeconds / 2628000).toInt()
            "$months ${if (months == 1) "month" else "months"} ago"
        }
        else -> {
            val years = (differenceInSeconds / 31536000).toInt()
            "$years ${if (years == 1) "year" else "years"} ago"
        }
    }
}

fun openBrowser(context: Context, uri: Uri) {
    val browserIntent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(browserIntent);
}
