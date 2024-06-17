package org.yrovas.linklater.ui.activity

import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.ui.screens.HomeScreen

@Inject
class MainActivity : AppActivity() {
    override val entryScreen = HomeScreen
}
