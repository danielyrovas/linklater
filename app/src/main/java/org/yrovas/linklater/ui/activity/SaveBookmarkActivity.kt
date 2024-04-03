package org.yrovas.linklater.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.generated.navgraphs.SaveBookmarkActivityNavGraph

@NavHostGraph
annotation class SaveBookmarkActivityGraph

class SaveBookmarkActivity : AppActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(SaveBookmarkActivityNavGraph)
    }
}
