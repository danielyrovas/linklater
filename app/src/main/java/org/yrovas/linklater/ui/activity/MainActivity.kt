package org.yrovas.linklater.ui.activity

import android.os.Bundle
import com.ramcosta.composedestinations.generated.navgraphs.RootNavGraph

class MainActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(RootNavGraph)
    }
}
