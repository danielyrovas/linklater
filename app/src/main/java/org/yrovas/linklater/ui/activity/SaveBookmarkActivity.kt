package org.yrovas.linklater.ui.activity

import android.content.Intent
import android.util.Log
import org.yrovas.linklater.ui.screens.NavEntryScreen
import org.yrovas.linklater.ui.screens.SaveBookmarkActivityScreen

annotation class SaveBookmarkActivityGraph

class SaveBookmarkActivity : AppActivity() {
    override val entryScreen = SaveBookmarkActivityScreen

    fun extractURL(): String {
        var s = intent.data.toString()
        if (s.isNotBlank() && s != "null") {
            Log.d("DEBUG/extract", "extractURL: intent.data")
            return s
        }
        s = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        if (s.isNotBlank()) {
            Log.d("DEBUG/extract", "extractURL: intent.extra_text")
            return s
        }
        s = intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: ""
        if (s.isNotBlank()) {
            Log.d("DEBUG/extract", "extractURL: intent.extra_subject")
            return s
        }
        return ""
    }
}
