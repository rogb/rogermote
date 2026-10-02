package com.rogbandroid.rogermote

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RemoteScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearSavedConfiguration() {
        ApplicationProvider.getApplicationContext<android.content.Context>()
            .getSharedPreferences("tv_configuration", android.content.Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        composeRule.activityRule.scenario.recreate()
    }

    @Test
    fun milestoneOneScreenAcceptsTvIpAddress() {
        composeRule.onNodeWithText("Samsung TV Remote").assertExists()
        composeRule.onNodeWithText("TV Setup").assertExists()
        composeRule.onNodeWithText("Status: Disconnected").assertExists()
        composeRule.onNodeWithText("Scan for TVs").assertIsEnabled()
        composeRule.onNodeWithText("Connect").assertIsNotEnabled()
        composeRule.onNodeWithText("Disconnect").assertIsNotEnabled()
        composeRule.onNodeWithText("Forget saved TV").assertIsNotEnabled()
        composeRule.onNodeWithText("Haptic feedback").assertExists()

        composeRule.onNodeWithText("TV IP").performTextInput("192.168.1.50")

        composeRule.onNodeWithText("TV IP").assertTextContains("192.168.1.50")
        composeRule.onNodeWithText("Connect").assertIsEnabled()
    }
}
