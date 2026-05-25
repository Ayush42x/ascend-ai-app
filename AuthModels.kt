package com.ascendai.domain.model

import com.google.firebase.auth.FirebaseUser

// ─── Domain User model ────────────────────────────────────────────────────────

data class AscendUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean,
    val providerId: String  // "google.com" | "password"
)

fun FirebaseUser.toAscendUser(): AscendUser = AscendUser(
    uid              = uid,
    email            = email,
    displayName      = displayName,
    photoUrl         = photoUrl?.toString(),
    isEmailVerified  = isEmailVerified,
    providerId       = providerData.firstOrNull()?.providerId ?: "password"
)

// ─── Auth result sealed class ─────────────────────────────────────────────────

sealed class AuthResult {
    data class Success(val user: AscendUser) : AuthResult()
    data class Error(val message: String, val cause: Throwable? = null) : AuthResult()
    object Loading : AuthResult()
}

// ─── Validation helpers ───────────────────────────────────────────────────────

object AuthValidator {

    fun validateEmail(email: String): String? {
        if (email.isBlank()) return "Email cannot be empty"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Enter a valid email"
        return null
    }

    fun validatePassword(password: String): String? {
        if (password.isBlank()) return "Password cannot be empty"
        if (password.length < 8) return "Password must be at least 8 characters"
        if (!password.any { it.isDigit() }) return "Password must contain a number"
        if (!password.any { it.isUpperCase() }) return "Password must contain an uppercase letter"
        return null
    }

    fun validateName(name: String): String? {
        if (name.isBlank()) return "Name cannot be empty"
        if (name.trim().length < 2) return "Name must be at least 2 characters"
        return null
    }

    fun validatePasswordMatch(password: String, confirm: String): String? {
        if (password != confirm) return "Passwords do not match"
        return null
    }
}
