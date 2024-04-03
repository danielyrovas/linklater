package org.yrovas.linklater

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import androidx.savedstate.SavedStateRegistryOwner
import com.ramcosta.composedestinations.generated.destinations.PreferencesScreenDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.yrovas.linklater.domain.*
import org.yrovas.linklater.ui.state.HomeScreenState
import org.yrovas.linklater.ui.state.PreferencesScreenState

object Prefs {
    val LINKDING_URL = stringPreferencesKey("linkding_url")
    val LINKDING_TOKEN = stringPreferencesKey("linkding_token")
}

class AppViewModelImpl : AppViewModel()

abstract class AppViewModel : ViewModel() {
    var bookmarkAPI: BookmarkAPI = EmptyBookmarkAPI()

    private val _bookmarkURL: MutableStateFlow<String> = MutableStateFlow("")
    var bookmarkURL = _bookmarkURL.asStateFlow()
    open fun saveBookmarkURL(context: Context, url: String) {
        _bookmarkURL.update { url }
        bookmarkAPI = LinkDingAPI(bookmarkURL.value, bookmarkAPIToken.value)
        viewModelScope.launch(Dispatchers.IO) {
            context.dataStore.edit { preferences ->
                preferences[Prefs.LINKDING_URL] = url
            }
        }
    }

    private val _bookmarkAPIToken: MutableStateFlow<String> = MutableStateFlow("")
    var bookmarkAPIToken = _bookmarkAPIToken.asStateFlow()
    open fun saveBookmarkAPIToken(context: Context, token: String) {
        _bookmarkAPIToken.update { token }
        bookmarkAPI = LinkDingAPI(bookmarkURL.value, bookmarkAPIToken.value)
        viewModelScope.launch(Dispatchers.IO) {
            context.dataStore.edit { preferences ->
                preferences[Prefs.LINKDING_TOKEN] = token
            }
        }
    }

    private fun loadPrefs(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            context.dataStore.data.first { preferences ->
                _bookmarkURL.update { preferences[Prefs.LINKDING_URL].orEmpty() }
                _bookmarkAPIToken.update { preferences[Prefs.LINKDING_TOKEN].orEmpty() }
                true
            }
            bookmarkAPI = LinkDingAPI(bookmarkURL.value, bookmarkAPIToken.value)
        }
    }

    open fun setup(context: Context) {
        loadPrefs(context)
    }
}

class PreviewAppViewModel : AppViewModel() {
    override fun saveBookmarkURL(context: Context, url: String) {}
    override fun saveBookmarkAPIToken(context: Context, token: String) {}
    override fun setup(context: Context) {}
}

//@Composable
//inline fun <reified VM : ViewModel> viewModel(
//    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(
//        LocalViewModelStoreOwner.current) {
//        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
//    },
//    savedStateRegistryOwner: SavedStateRegistryOwner = LocalSavedStateRegistryOwner.current
//): VM {
//    return androidx.lifecycle.viewmodel.compose.viewModel(
//        viewModelStoreOwner = viewModelStoreOwner,
//        factory = ViewModelFactory(
//            owner = savedStateRegistryOwner,
//            defaultArgs = (savedStateRegistryOwner as? NavBackStackEntry)?.arguments,
//        )
//    )
//}

//class ViewModelFactory(
//    owner: SavedStateRegistryOwner,
//    defaultArgs: Bundle?,
//) : AbstractSavedStateViewModelFactory(
//    owner,
//    defaultArgs
//) {
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(
//        key: String,
//        modelClass: Class<T>,
//        handle: SavedStateHandle
//    ): T {
//        return when (modelClass) {
//            PreferencesScreenState::class.java -> PreferencesScreenState()
//            HomeScreenState::class.java -> HomeScreenState()
//
//            else -> throw RuntimeException("Unknown view model $modelClass")
//        } as T
//    }
//}
