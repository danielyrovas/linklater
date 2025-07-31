package org.yrovas.linklater.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.Log
import org.yrovas.linklater.ui.screens.Destination
import org.yrovas.linklater.ui.theme.AppTheme

@Inject
class MainActivity : AppActivity() {
    override val entryDestination: Destination = Destination.Home

}
