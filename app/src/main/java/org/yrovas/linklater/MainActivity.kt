package org.yrovas.linklater

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.ActivityDestination
import com.ramcosta.composedestinations.navigation.dependency
import kotlinx.coroutines.launch
import org.yrovas.linklater.ui.theme.AppTheme

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

@ActivityDestination
class MainActivity : ComponentActivity() {
    private val mainActivityState: MainActivityState by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        lifecycleScope.launch { mainActivityState.setup(this@MainActivity) }
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DestinationsNavHost(navGraph = NavGraphs.root,
                        dependenciesContainerBuilder = {
                            dependency(NavGraphs.root) { mainActivityState }
                        })
                }
            }
        }
    }
}
