package org.yrovas.linklater
import kotlinx.datetime.Instant
import kotlin.math.abs

fun relativeTime(timestamp: Instant, now: Instant): String {
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
fun main() {
//    val ts = "2024-02-06T00:05:35.570735Z"
//    val t = Instant.parse(ts)
//    println(relativeTime(t, Clock.System.now()))
//    val nt = (listOf("name", "name1", "name2", "name3") + ("name hi name3 tag".split(" ") ?: emptyList())).distinct()
//    val url: String? = null
//
//    val nt: String?  = url?.ifBlank { "Gargle" } ?: url ?: "Default"
//
//    println("val: $nt")
}
