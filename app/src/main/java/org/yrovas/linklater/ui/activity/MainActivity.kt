package org.yrovas.linklater.ui.activity

import com.ramcosta.composedestinations.generated.navgraphs.RootNavGraph
import com.ramcosta.composedestinations.spec.NavHostGraphSpec
import me.tatarka.inject.annotations.Inject

@Inject
class MainActivity : AppActivity() {
    override val navGraph: NavHostGraphSpec = RootNavGraph
}
