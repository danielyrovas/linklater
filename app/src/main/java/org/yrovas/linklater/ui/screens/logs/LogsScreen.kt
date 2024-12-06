package org.yrovas.linklater.ui.screens.logs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.AppViewModel
import org.yrovas.linklater.ui.common.Frame
import org.yrovas.linklater.ui.screens.LocalBackStack
import org.yrovas.linklater.ui.theme.padding

typealias LogsScreen = @Composable () -> Unit

@Inject
@Composable
fun LogsScreen(logsModel: () -> LogsModel) {
    val backStack = LocalBackStack.current
    val logsModel = viewModel { logsModel() }
    val logs by logsModel.logs.collectAsState()

    Frame(
        title = "Logs",
        back = { backStack.removeLastOrNull() }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(padding.md),
            verticalArrangement = Arrangement.spacedBy(padding.sm),
        ) {
            items(logs) {
                Text(
                    text = it.message,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding.md),
                )
            }
        }
    }
}
