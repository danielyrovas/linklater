package org.yrovas.linklater.ui.activity

import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.ui.screens.Destination

@Inject
class MainActivity : AppActivity() {
    override val entryScreen = Destination.Home
}
