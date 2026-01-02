package com.yogeshpaliyal.common.data

import com.yogeshpaliyal.common.constants.AccountType
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountModelTest {

    @Test
    fun testApiKeyAccountCreation() {
        // Arrange
        val apiKeyValue = "my-secret-api-key"
        val apiKeyScope = "read/write"
        val apiKeyExpiry = 1735689600000L // Some timestamp

        // Act
        val accountModel = AccountModel(
            type = AccountType.API_KEY,
            apiKeyValue = apiKeyValue,
            apiKeyScope = apiKeyScope,
            apiKeyExpiry = apiKeyExpiry
        )

        // Assert
        assertEquals("Account Type should be API_KEY", AccountType.API_KEY, accountModel.type)
        assertEquals("API Key Value should match", apiKeyValue, accountModel.apiKeyValue)
        assertEquals("API Key Scope should match", apiKeyScope, accountModel.apiKeyScope)
        assertEquals("API Key Expiry should match", apiKeyExpiry, accountModel.apiKeyExpiry)
    }

    @Test
    fun testSameAccountModelUsedForPasswordAndApiKey() {
        // Arrange & Act
        val passwordAccount = AccountModel(
            type = AccountType.DEFAULT,
            username = "user@example.com",
            password = "password123"
        )
        val apiKeyAccount = AccountModel(
            type = AccountType.API_KEY,
            apiKeyValue = "sk_test_123"
        )

        // Assert - Both use the same AccountModel class
        assertEquals("Both accounts should use AccountModel class",
            passwordAccount::class, apiKeyAccount::class)
    }

    @Test
    fun testApiKeyFieldsAreNullableInPasswordAccounts() {
        // Arrange & Act
        val passwordAccount = AccountModel(
            type = AccountType.DEFAULT,
            username = "user@example.com",
            password = "password123"
        )

        // Assert - API key fields should be null in password accounts
        assertEquals("apiKeyValue should be null", null, passwordAccount.apiKeyValue)
        assertEquals("apiKeyScope should be null", null, passwordAccount.apiKeyScope)
        assertEquals("apiKeyExpiry should be null", null, passwordAccount.apiKeyExpiry)
    }

    @Test
    fun testAccountModelCopyPreservesApiKeyFields() {
        // Arrange
        val originalAccount = AccountModel(
            type = AccountType.API_KEY,
            title = "Original Key",
            apiKeyValue = "sk_original_123",
            apiKeyScope = "read"
        )

        // Act
        val updatedAccount = originalAccount.copy(
            title = "Updated Key",
            apiKeyScope = "read,write"
        )

        // Assert
        assertEquals("Type should be preserved", AccountType.API_KEY, updatedAccount.type)
        assertEquals("Title should be updated", "Updated Key", updatedAccount.title)
        assertEquals("apiKeyValue should be preserved", "sk_original_123", updatedAccount.apiKeyValue)
        assertEquals("apiKeyScope should be updated", "read,write", updatedAccount.apiKeyScope)
    }
}
