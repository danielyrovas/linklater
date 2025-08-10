package org.yrovas.linklater.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.yrovas.linklater.AppComponent
import org.yrovas.linklater.Log
import org.yrovas.linklater.create
import org.yrovas.linklater.ui.screens.Destination
import org.yrovas.linklater.ui.theme.AppTheme

abstract class AppActivity : ComponentActivity() {

    protected abstract val entryDestination: Destination

    protected val appComponent by lazy(LazyThreadSafetyMode.NONE) {
        AppComponent::class.create(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        enableEdgeToEdge()
        Log.v { "Initialised Android app" }

        setContent {
            AppTheme { appComponent.navigationHost(entryDestination) }
        }
    }
}
