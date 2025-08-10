package org.yrovas.linklater.ui.activity

import android.content.Intent
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
            Log.d { "Extracted URL from Intent extra text: $s" }
            return s
        }
        s = intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: ""
        if (s.isNotBlank()) {
            Log.d { "Extracted URL from Intent extra subject: $s" }
            return s
        }
        Log.w { "Could not extract URL from Intent" }
        return ""
    }
}
