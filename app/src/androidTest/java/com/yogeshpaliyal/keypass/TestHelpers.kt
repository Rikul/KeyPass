package com.yogeshpaliyal.keypass

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.rules.ActivityScenarioRule

/**
 * Helper function to set up the master password during tests.
 *
 * Handles the initial master password creation flow by:
 * 1. Entering the master password
 * 2. Clicking Continue
 * 3. Confirming the master password
 * 4. Clicking Continue again
 *
 * @param composeTestRule The compose test rule for UI interaction
 * @param password The master password to set (defaults to "TestPassword123!")
 */
fun <A : androidx.activity.ComponentActivity> setupMasterPassword(
    composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<A>, A>,
    password: String = "TestPassword123!"
) {
    // Wait for password input to appear
    composeTestRule.waitUntil(timeoutMillis = 5000) {
        try {
            composeTestRule.onNodeWithTag("masterPasswordInput").assertExists()
            true
        } catch (e: AssertionError) {
            false
        }
    }

    // Step 1: Enter master password
    composeTestRule.onNodeWithTag("masterPasswordInput")
        .assertIsDisplayed()
        .performTextInput(password)

    // Close soft keyboard to make Continue button visible
    closeSoftKeyboard()
    composeTestRule.waitForIdle()

    // Step 2: Click Continue
    composeTestRule.onNodeWithTag("btnContinue")
        .assertIsDisplayed()
        .performClick()

    // Check if we're already logged in (btnAdd appears) or need to confirm password
    composeTestRule.waitForIdle()
    Thread.sleep(500) // Small delay to let UI settle

    val isLoggedIn = try {
        composeTestRule.onNodeWithTag("btnAdd").assertExists()
        true
    } catch (e: AssertionError) {
        false
    }

    // If already logged in, we're done
    if (isLoggedIn) {
        return
    }

    // Otherwise, we're on the confirm password screen
    // Wait for confirmation screen
    composeTestRule.waitUntil(timeoutMillis = 2000) {
        try {
            composeTestRule.onNodeWithTag("masterPasswordInput").assertExists()
            true
        } catch (e: AssertionError) {
            false
        }
    }

    // Step 3: Confirm master password
    composeTestRule.onNodeWithTag("masterPasswordInput")
        .assertIsDisplayed()
        .performTextInput(password)

    // Close soft keyboard again before clicking Continue
    closeSoftKeyboard()
    composeTestRule.waitForIdle()

    // Step 4: Click Continue to finish setup
    composeTestRule.onNodeWithTag("btnContinue")
        .assertIsDisplayed()
        .performClick()

    // Wait for home screen to appear (indicated by btnAdd)
    composeTestRule.waitUntil(timeoutMillis = 5000) {
        try {
            composeTestRule.onNodeWithTag("btnAdd").assertExists()
            true
        } catch (e: AssertionError) {
            false
        }
    }
}

/**
 * Helper function to close the soft keyboard during tests.
 */
fun closeSoftKeyboard() {
    Espresso.closeSoftKeyboard()
}
