package org.yrovas.linklater

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.ActivityDestination
import com.ramcosta.composedestinations.navigation.dependency
import kotlinx.coroutines.*
import org.yrovas.linklater.ui.theme.AppTheme

val globalScope = CoroutineScope(SupervisorJob())

@ActivityDestination
class SaveBookmarkActivity : ComponentActivity() {
    private val saveActivityState: SaveActivityState by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val url = extractURL(intent)
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Log.d("DEBUG/nav", "checkingIntent URL: $url")
        saveActivityState.updateBookmark(url)
        lifecycleScope.launch {
            saveActivityState.setup(this@SaveBookmarkActivity)
        }

        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DestinationsNavHost(navGraph = NavGraphs.saveBookmark,
                        dependenciesContainerBuilder = {
                            dependency(NavGraphs.saveBookmark) { saveActivityState }
                        })
                }
            }
        }
    }

    fun launch(function: suspend () -> Unit) {
        globalScope.launch {
            function()
        }
    }

    private fun extractURL(intent: Intent): String {
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

//        Log.d("DEBUG/extract", "extractURL: clipBoard")
        return "" // clipboardData()
    }

//    private fun clipboardData(): String {
//        val clipboardManager =
//            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//        val clip: ClipData? = clipboardManager.primaryClip
//        var txt: String = (clip?.getItemAt(0)?.text ?: "").toString()
//        if (txt.isNotBlank()) return txt
//        txt = (clip?.getItemAt(1)?.text ?: "").toString()
//        if (txt.isNotBlank()) return txt
//
//        return ""
//    }
}
