package com.yogeshpaliyal.keypass.ui.auth

fun isMasterPasswordValid(password: String): Boolean {
    if (password.length < 10) {
        return false
    }

    val hasUppercase = password.any { it.isUpperCase() }
    val hasLowercase = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }

    return hasUppercase && hasLowercase && hasDigit && hasSpecial
}
