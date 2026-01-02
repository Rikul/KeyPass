package com.yogeshpaliyal.keypass.ui.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.common.constants.AccountType
import com.yogeshpaliyal.common.constants.ScannerType
import com.yogeshpaliyal.common.data.AccountModel
import com.yogeshpaliyal.common.utils.PasswordGenerator
import com.yogeshpaliyal.keypass.R
import com.yogeshpaliyal.keypass.ui.commonComponents.KeyPassInputField
import com.yogeshpaliyal.keypass.ui.commonComponents.PasswordTrailingIcon
import com.yogeshpaliyal.keypass.ui.nav.LocalUserSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Fields(
    modifier: Modifier = Modifier,
    accountModel: AccountModel,
    updateAccountModel: (newAccountModel: AccountModel) -> Unit,
    copyToClipboardClicked: (String) -> Unit,
    scanClicked: (scannerType: Int) -> Unit
) {
    val passwordConfig = LocalUserSettings.current.passwordConfig
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = accountModel.apiKeyExpiry)
    val showDatePicker = remember { mutableStateOf(false) }

    if (showDatePicker.value) {
        LaunchedEffect(Unit) {
            datePickerState.selectedDateMillis = accountModel.apiKeyExpiry
        }
        DatePickerDialog(
            modifier = Modifier.testTag("datePickerDialog"),
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                TextButton(modifier = Modifier.testTag("btnConfirmDate"), onClick = {
                    datePickerState.selectedDateMillis?.let {
                        updateAccountModel(accountModel.copy(apiKeyExpiry = it))
                    }
                    showDatePicker.value = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(modifier = Modifier.testTag("btnCancelDate"), onClick = { showDatePicker.value = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(modifier = Modifier.testTag("datePicker"), state = datePickerState)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KeyPassInputField(
            modifier = Modifier.testTag("accountName"),
            placeholder = R.string.account_name,
            value = accountModel.title,
            setValue = {
                updateAccountModel(accountModel.copy(title = it))
            },
            copyToClipboardClicked = copyToClipboardClicked
        )

        if (accountModel.type == AccountType.API_KEY) {
            KeyPassInputField(
                modifier = Modifier.testTag("apiKeyValue"),
                placeholder = R.string.api_key_value,
                value = accountModel.apiKeyValue,
                setValue = {
                    updateAccountModel(accountModel.copy(apiKeyValue = it))
                },
                copyToClipboardClicked = copyToClipboardClicked
            )
        } else {
            KeyPassInputField(
                modifier = Modifier.testTag("username"),
                placeholder = R.string.username_email_phone,
                value = accountModel.username,
                setValue = {
                    updateAccountModel(accountModel.copy(username = it))
                },
                copyToClipboardClicked = copyToClipboardClicked
            )

            Column {
                val passwordVisible = rememberSaveable { mutableStateOf(false) }

                val visualTransformation =
                    if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation()

                KeyPassInputField(
                    modifier = Modifier.testTag("password"),
                    placeholder = R.string.password,
                    value = accountModel.password,
                    setValue = {
                        updateAccountModel(accountModel.copy(password = it))
                    },
                    trailingIcon = {
                        PasswordTrailingIcon(passwordVisible.value) {
                            passwordVisible.value = it
                        }
                    },
                    leadingIcon = if (accountModel.id != null) {
                        null
                    } else {
                        (
                            {
                                IconButton(
                                    onClick = {
                                        updateAccountModel(accountModel.copy(password = PasswordGenerator(passwordConfig).generatePassword()))
                                    }
                                ) {
                                    Icon(
                                        painter = rememberVectorPainter(image = Icons.Rounded.Refresh),
                                        contentDescription = ""
                                    )
                                }
                            }
                            )
                    },
                    visualTransformation = visualTransformation,
                    copyToClipboardClicked = copyToClipboardClicked
                )
                Button(onClick = { scanClicked(ScannerType.Password) }) {
                    Row {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_twotone_qr_code_scanner_24),
                            contentDescription = ""
                        )
                        Text(text = stringResource(id = R.string.scan_password))
                    }
                }
            }

            KeyPassInputField(
                modifier = Modifier.testTag("secretKey"),
                placeholder = R.string.secret_key,
                value = accountModel.secret,
                setValue = {
                    updateAccountModel(accountModel.copy(secret = it))
                },
                trailingIcon = {
                    IconButton(onClick = {
                        scanClicked(ScannerType.Secret)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_twotone_qr_code_scanner_24),
                            contentDescription = ""
                        )
                    }
                }
            )
        }

        KeyPassInputField(
            modifier = Modifier.testTag("website"),
            placeholder = if (accountModel.type == AccountType.API_KEY) R.string.website_url else R.string.website_url_optional,
            value = accountModel.site,
            setValue = {
                updateAccountModel(accountModel.copy(site = it))
            },
            copyToClipboardClicked = copyToClipboardClicked
        )

        if (accountModel.type == AccountType.API_KEY) {
            KeyPassInputField(
                modifier = Modifier.testTag("apiKeyScope"),
                placeholder = R.string.api_key_scope,
                value = accountModel.apiKeyScope,
                setValue = {
                    updateAccountModel(accountModel.copy(apiKeyScope = it))
                },
                copyToClipboardClicked = copyToClipboardClicked
            )

            KeyPassInputField(
                modifier = Modifier.testTag("apiKeyExpiry"),
                placeholder = R.string.api_key_expiry,
                value = accountModel.apiKeyExpiry?.let {
                    try {
                        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(it))
                    } catch (e: Exception) {
                        ""
                    }
                } ?: "",
                setValue = {
                    // Read only
                },
                readOnly = true,
                trailingIcon = {
                    IconButton(modifier = Modifier.testTag("btnSelectDate"), onClick = { showDatePicker.value = true }) {
                        Icon(imageVector = Icons.Rounded.DateRange, contentDescription = "Select Date")
                    }
                },
                copyToClipboardClicked = copyToClipboardClicked
            )
        }

        KeyPassInputField(
            modifier = Modifier.testTag("tags"),
            placeholder = R.string.tags_comma_separated_optional,
            value = accountModel.tags,
            setValue = {
                updateAccountModel(accountModel.copy(tags = it))
            },
            copyToClipboardClicked = copyToClipboardClicked
        )

        KeyPassInputField(
            modifier = Modifier.testTag("notes"),
            placeholder = R.string.notes_optional,
            value = accountModel.notes,
            setValue = {
                updateAccountModel(accountModel.copy(notes = it))
            },
            copyToClipboardClicked = copyToClipboardClicked
        )
    }
}
