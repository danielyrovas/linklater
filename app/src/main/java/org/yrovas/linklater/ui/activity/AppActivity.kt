package org.yrovas.linklater.ui.activity

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.spec.NavHostGraphSpec
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.*
import org.yrovas.linklater.AppViewModel
import org.yrovas.linklater.AppViewModelImpl
import org.yrovas.linklater.domain.BookmarkAPI
import org.yrovas.linklater.domain.LinkDingAPI
import org.yrovas.linklater.ui.theme.AppTheme

@Scope
annotation class AppScope

@Component
abstract class AppComponent(
    @get:Provides val app: Application,
) {
    companion object {
        private var instance: AppComponent? = null
        fun getInstance(context: Context) =
            instance ?: AppComponent::class.create(
                context.applicationContext as Application,
            ).also { instance = it }
    }
}

@AppScope
@Provides
fun provideHttpClient(): HttpClient = HttpClient(Android) {
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                Log.i("Ktor =>", message)
            }
        }
        level = LogLevel.ALL
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    install(DefaultRequest) {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
    }
}

@AppScope
@Provides
fun provideBookmarkAPI(client: HttpClient): BookmarkAPI {
    return LinkDingAPI(client)
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

@Provides
fun provideDataStore(context: Context): DataStore<Preferences> = context.dataStore

inline fun <reified VM : ViewModel> AppActivity.viewModel(crossinline factory: () -> VM): Lazy<VM> =
    viewModels {
        viewModelFactory { addInitializer(VM::class) { factory() } }
    }

@Inject
abstract class AppActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModel { AppViewModelImpl() }

    fun launch(job: suspend () -> Unit) {
        lifecycleScope.launch { job() }
    }

    fun setContent(navGraph: NavHostGraphSpec) {
        val appComponent = AppComponent::class.create(applicationContext as Application)
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

//        appViewModel.loadPrefs()


        setContent {
            val snackState = remember { SnackbarHostState() }
//            val homeScreenState = viewModel { homeScreenState() }

            AppTheme {
                DestinationsNavHost(
                    navGraph = navGraph,
                    dependenciesContainerBuilder = {
//                        navGraph(navGraph) {
                            dependency(snackState)
                        dependency(appViewModel)
//                            dependency(viewModel<SaveBookmarkScreenState>())
//                            dependency(homeScreenState)
//                            dependency(viewModel<PreferencesScreenState>())
//                        }
                    })
            }
        }
    }
}
