package com.yogeshpaliyal.keypass.ui.nav

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.`is`
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.setupMasterPassword
import com.yogeshpaliyal.keypass.closeSoftKeyboard
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject



@HiltAndroidTest
class ApiKeyAccountTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var composeTestRule = createAndroidComposeRule<DashboardComposeActivity>()

    @Inject
    lateinit var appDatabase: com.yogeshpaliyal.common.AppDatabase

    @Before
    fun setUp() {
        hiltRule.inject()

        // Clear database tables
        appDatabase.clearAllTables()

        // Set up master password before each test
        setupMasterPassword(composeTestRule)
    }

    @Test
    fun testAccountTypeSelectionDialogAppears() {
        // Click on the + button (Floating Action Button)
        composeTestRule.onNodeWithTag("btnAdd").performClick()

        // Verify the dialog appears
        composeTestRule.onNodeWithTag("accountTypeDialog").assertIsDisplayed()

        // Verify "Password Account" option is displayed
        composeTestRule.onNodeWithTag("btnPasswordAccount").assertIsDisplayed()

        // Verify "API Key Account" option is displayed
        composeTestRule.onNodeWithTag("btnApiKeyAccount").assertIsDisplayed()
    }

    @Test
    fun testNavigateToCreateApiKeyScreen() {
        // Click on the + button
        composeTestRule.onNodeWithTag("btnAdd").performClick()

        // Click on "API Key Account"
        composeTestRule.onNodeWithTag("btnApiKeyAccount").performClick()

        // Verify "Create API Key" screen title
        composeTestRule.onNodeWithText("Create API Key").assertIsDisplayed()

        // Verify fields
        composeTestRule.onNodeWithTag("accountName").assertIsDisplayed()
        composeTestRule.onNodeWithTag("apiKeyValue").assertIsDisplayed()
        composeTestRule.onNodeWithTag("website").assertIsDisplayed()
        composeTestRule.onNodeWithTag("apiKeyScope").assertIsDisplayed()
        composeTestRule.onNodeWithTag("apiKeyExpiry").assertIsDisplayed()
        composeTestRule.onNodeWithTag("notes").assertIsDisplayed()

        // Verify username and password are NOT displayed
        composeTestRule.onNodeWithTag("username").assertDoesNotExist()
        composeTestRule.onNodeWithTag("password").assertDoesNotExist()
    }

    @Test
    fun testCreateAndVerifyListDisplaysApiKeyAccount() {
        val accountName = "My API Key Test"
        val apiKeyValue = "secret-key-123"
        val scope = "read-only-scope"

        // Click on the + button
        composeTestRule.onNodeWithTag("btnAdd").performClick()

        // Click on "API Key Account"
        composeTestRule.onNodeWithTag("btnApiKeyAccount").performClick()

        // Fill in the fields
        composeTestRule.onNodeWithTag("accountName").performTextInput(accountName)
        composeTestRule.onNodeWithTag("apiKeyValue").performTextInput(apiKeyValue)
        closeSoftKeyboard()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("website").performTextInput("https://example.com")
        composeTestRule.onNodeWithTag("apiKeyScope").performTextInput(scope)

        closeSoftKeyboard()
        composeTestRule.waitForIdle()

        // Select Expiry Date
        composeTestRule.onNodeWithTag("btnSelectDate").performClick()
        composeTestRule.waitForIdle()

        // Click the edit/input mode icon in the DatePicker (switch to text input)
        composeTestRule.onNodeWithContentDescription("Switch to text input mode").performClick()
        composeTestRule.waitForIdle()

        // Find the text input field in the date picker dialog and enter date
        // Format: MM/DD/YYYY (12/31/2030)
        composeTestRule.onNodeWithContentDescription("Date, MM/DD/YYYY").performTextInput("12312030")

        closeSoftKeyboard()
        composeTestRule.waitForIdle()

        // Confirm the date
        composeTestRule.onNodeWithTag("btnConfirmDate").performClick()

        // Save
        composeTestRule.onNodeWithTag("save").performClick()

        // Verify account name is displayed in list
        composeTestRule.onNodeWithText(accountName, substring = true).assertIsDisplayed()

        // Verify the scope is displayed
        composeTestRule.onNodeWithText(scope, substring = true).assertIsDisplayed()

        // Verify a date is displayed (may be off by one day due to timezone conversion)
        // DatePicker stores at midnight UTC, but display uses local timezone
        composeTestRule.onNodeWithText("2030-12-3", substring = true).assertIsDisplayed()
    }

    @Test
    fun testEditApiKeyAccount() {
        val accountName = "Edit API Key Test"
        val apiKeyValue = "edit-secret-key"
        val scope = "edit-scope"
        val notes = "These are some notes."

        // 1. Create the account
        composeTestRule.onNodeWithTag("btnAdd").performClick()
        composeTestRule.onNodeWithTag("btnApiKeyAccount").performClick()

        composeTestRule.onNodeWithTag("accountName").performTextInput(accountName)
        composeTestRule.onNodeWithTag("apiKeyValue").performTextInput(apiKeyValue)
        
        closeSoftKeyboard()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("website").performTextInput("https://edit.example.com")
        composeTestRule.onNodeWithTag("apiKeyScope").performTextInput(scope)

        closeSoftKeyboard()
        composeTestRule.waitForIdle()

        // Select Expiry Date
        composeTestRule.onNodeWithTag("btnSelectDate").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Switch to text input mode").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Date, MM/DD/YYYY").performTextInput("12312030")
        closeSoftKeyboard()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btnConfirmDate").performClick()


        composeTestRule.onNodeWithTag("notes").performTextInput(notes)

        closeSoftKeyboard()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("save").performClick()

        // 2. Find and click the account in the list
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(accountName).performClick()

        // 3. Verify we are on the Edit screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Edit API Key").assertIsDisplayed()

        // 4. Verify fields are populated
        composeTestRule.onNodeWithTag("accountName").assertTextContains(accountName)
        composeTestRule.onNodeWithTag("apiKeyValue").assertTextContains(apiKeyValue)
        composeTestRule.onNodeWithTag("apiKeyScope").assertTextContains(scope)

        
        // Verify date (checking substring to handle potential timezone shifts)
        composeTestRule.onNodeWithTag("apiKeyExpiry").assertTextContains("2030-12-3", substring = true)

        composeTestRule.onNodeWithTag("notes").assertTextContains(notes)

    }

    @Test
    fun testUpdateApiKeyAccount() {
        val accountName = "Update API Key Test"
        val apiKeyValue = "original-secret"
        val originalScope = "original-scope"
        val originalNotes = "original-notes"

        val updatedScope = "updated-scope"
        val updatedNotes = "updated-notes"

        // 1. Create the account
        composeTestRule.onNodeWithTag("btnAdd").performClick()
        composeTestRule.onNodeWithTag("btnApiKeyAccount").performClick()

        composeTestRule.onNodeWithTag("accountName").performTextInput(accountName)
        composeTestRule.onNodeWithTag("apiKeyValue").performTextInput(apiKeyValue)

        closeSoftKeyboard()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithTag("website").performTextInput("https://update.example.com")
        composeTestRule.onNodeWithTag("apiKeyScope").performTextInput(originalScope)
        composeTestRule.onNodeWithTag("notes").performTextInput(originalNotes)

        closeSoftKeyboard()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("save").performClick()

        // 2. Find and click the account to edit
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(accountName).performClick()

        // 3. Update fields
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("apiKeyScope").performTextReplacement(updatedScope)
        composeTestRule.onNodeWithTag("notes").performTextReplacement(updatedNotes)

        closeSoftKeyboard()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("save").performClick()

        // 4. Find and click the account again to verify
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(accountName).performClick()

        // 5. Verify updates
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("apiKeyScope").assertTextContains(updatedScope)
        composeTestRule.onNodeWithTag("notes").assertTextContains(updatedNotes)
    }

    @Test
    fun testCreateApiKeyRequiredFieldsValidation() {
        // 1. Navigate to Create API Key screen
        composeTestRule.onNodeWithTag("btnAdd").performClick()
        composeTestRule.onNodeWithTag("btnApiKeyAccount").performClick()

        composeTestRule.waitForIdle()

        // 2. Attempt to save with all fields blank
        composeTestRule.onNodeWithTag("save").performClick()
        
        // Verify we are still on the screen
        composeTestRule.onNodeWithText("Create API Key").assertIsDisplayed()
        
        // 3. Fill Account Name, leave others blank
        composeTestRule.onNodeWithTag("accountName").performTextInput("Test Account")
        closeSoftKeyboard()
        composeTestRule.onNodeWithTag("save").performClick()
            
        composeTestRule.onNodeWithText("Create API Key").assertIsDisplayed()
        
        // 4. Fill API Key, leave Website blank
        composeTestRule.onNodeWithTag("apiKeyValue").performTextInput("secret-key")
        closeSoftKeyboard()
        composeTestRule.onNodeWithTag("save").performClick()
            
        composeTestRule.onNodeWithText("Create API Key").assertIsDisplayed()
        
        // 5. Fill Website (all required now filled)
        composeTestRule.onNodeWithTag("website").performTextInput("https://example.com")
        closeSoftKeyboard()
        composeTestRule.onNodeWithTag("save").performClick()
        
        // 6. Verify we navigated away (Create API Key screen is gone)
        composeTestRule.onNodeWithText("Create API Key").assertDoesNotExist()
    }
}
