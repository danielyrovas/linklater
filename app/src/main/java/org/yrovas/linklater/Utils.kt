package org.yrovas.linklater

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.Instant
import org.yrovas.linklater.domain.APIError
import kotlin.math.abs

fun checkURL(url: String) = url.contains(Regex("^https?://.+[.].+"))
fun checkBookmarkAPIToken(token: String) = token.length in 10..120

@Composable
fun timeAgo(timestamp: Instant, now: Instant): String {
    val differenceInSeconds = abs((now - timestamp).inWholeSeconds)
    return when {
        differenceInSeconds < 60 -> stringResource(R.string.just_now)
        differenceInSeconds < 3600 -> stringResource(
            R.string.min_ago, (differenceInSeconds / 60).toInt()
        )

        differenceInSeconds < 86400 -> stringResource(
            R.string.hours_ago, (differenceInSeconds / 3600).toInt()
        )
        differenceInSeconds < 2628000 -> {
            val days = (differenceInSeconds / 86400).toInt()
            if (days == 1) stringResource(R.string.day_ago, days)
            else stringResource(R.string.days_ago, days)
        }
        differenceInSeconds < 31536000 -> {
            val months = (differenceInSeconds / 2628000).toInt()
            if (months == 1) stringResource(R.string.month_ago, months)
            else stringResource(R.string.months_ago, months)
        }
        else -> {
            val years = (differenceInSeconds / 31536000).toInt()
            if (years == 1) stringResource(R.string.year_ago, years)
            else stringResource(R.string.years_ago, years)
        }
    }
}

fun Context.openUri(uri: Uri) {
    val browserIntent = Intent(Intent.ACTION_VIEW, uri)
    startActivity(browserIntent)
}

fun Context.readClipboard(): String {
    val clipboardManager =
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    if (clipboardManager.hasPrimaryClip()) {
        val clipData = clipboardManager.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            return clipData.getItemAt(0).text?.toString() ?: ""
        }
    }
    return ""
}

fun Context.onBackPressed() {
    @Suppress("DEPRECATION") (this as Activity).onBackPressed()
}

fun Context.getAppVersion(): String {
    return try {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        packageInfo.versionName
    } catch (e: Exception) {
        "Not Found"
    }
}

@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class ThemePreview

fun String.intoTags(): List<String> =
    split(" ").filter { it.isNotBlank() }.distinct()

suspend fun SnackbarHostState.show(error: APIError) {
    when (error) {
        APIError.NO_CONNECTION -> showSnackbar("Could not connect to LinkDing")
        APIError.INCORRECT_AUTH -> showSnackbar("Invalid Token")
        APIError.INCORRECT_ENDPOINT -> showSnackbar("Invalid API Endpoint")
        APIError.NO_AUTH_PROVIDED -> showSnackbar("No authentication provided")
    }
}
