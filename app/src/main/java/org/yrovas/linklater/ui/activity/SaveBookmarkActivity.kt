package org.yrovas.linklater.ui.activity

import android.content.Intent
import android.util.Patterns
import org.yrovas.linklater.Log
import org.yrovas.linklater.ui.screens.Destination

class SaveBookmarkActivity : AppActivity() {
    override val entryDestination: Destination = Destination.SaveBookmarkActivity

    fun extractURL(): String {
        var s = intent.data.toString()
        if (s.isNotBlank() && s != "null") {
            Log.d { "Extracted URL from Intent data: $s" }
            return s
        }
        s = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        if (s.isNotBlank()) {
            val url = extractURL(s)
            Log.d { "Extracted URL from Intent extra text: $url (original: $s)" }
            return url
        }
        s = intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: ""
        if (s.isNotBlank()) {
            val url = extractURL(s)
            Log.d { "Extracted URL from Intent extra subject: $url (original: $s)" }
            return url
        }
        Log.w { "Could not extract URL from Intent" }
        return ""
    }

    /**
     * Extracts a URL from text that may contain additional content like titles.
     * Some apps (e.g., Google News) share text in the format:
     * "Article Title\n\nhttps://example.com/article"
     */
    private fun extractURL(text: String): String {
        val trimmed = text.trim()

        // Use the system's URL pattern to find the first valid web link
        val matcher = Patterns.WEB_URL.matcher(trimmed)
        if (matcher.find()) {
            return matcher.group()
        }
        return ""
    }
}
