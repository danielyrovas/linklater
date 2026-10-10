package org.yrovas.linklater

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.yrovas.linklater.ui.activity.MainActivity

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
class MainActivityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun waitForHomeScreen() {
        composeRule.waitUntilExactlyOneExists(
            hasText("Showing", substring = true),
            timeoutMillis = 5_000,
        )
    }

    @Test
    fun bootsToHomeScreenAndOpensPreferences() {
        waitForHomeScreen()
        composeRule.onNodeWithText("Showing", substring = true).assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Preferences").performClick()

        composeRule.waitUntilExactlyOneExists(hasText("LinkDing Account"), timeoutMillis = 5_000)
    }

    @Test
    fun addBookmarkOpensSaveScreen() {
        waitForHomeScreen()

        composeRule.onNodeWithText("Add bookmark", useUnmergedTree = true).performClick()

        composeRule.waitUntilExactlyOneExists(hasText("Tags"), timeoutMillis = 5_000)
    }
}
