package org.yrovas.linklater.ui.activity

import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.ui.screens.home.HomeDestination

@Inject
class MainActivity : AppActivity() {
    override val entryScreen = HomeDestination
}
