package org.yrovas.linklater.ui.screens.saveBookmark

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.ui.activity.SaveBookmarkActivity

typealias SaveBookmarkActivityScreen = @Composable () -> Unit

@Inject
@Composable
fun SaveBookmarkActivityScreen(
    saveBookmarkModel: (bookmarkParam: BookmarkParam) -> SaveBookmarkModel,
    context: Context = LocalContext.current
) = SaveBookmarkScreen(
    bookmarkParam = BookmarkParam.Save(url = (context as SaveBookmarkActivity).extractURL()),
    saveBookmarkModel = saveBookmarkModel,
    back = { context.onBackPressedDispatcher.onBackPressed() },
    exitOnSuccess = true
)
