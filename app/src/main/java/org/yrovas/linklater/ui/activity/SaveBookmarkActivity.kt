package org.yrovas.linklater

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import org.yrovas.linklater.ui.screens.NavGraphs
import org.yrovas.linklater.ui.theme.AppTheme

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
        setContent(NavGraphs.saveBookmarkActivity)
    }
}
