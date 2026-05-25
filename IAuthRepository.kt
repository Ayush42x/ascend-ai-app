package com.ascendai.domain.repository

import android.app.Activity
import com.ascendai.domain.model.AscendUser
import com.ascendai.domain.model.AuthResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.Flow

interface IAuthRepository {

    /** Emits the current signed-in user, or null if signed out. Survives config changes. */
    val currentUserFlow: Flow<AscendUser?>

    /** Synchronous snapshot — use for initial guard checks only. */
    val currentUser: AscendUser?

    /** Sign in with email + password. */
    suspend fun signInWithEmail(email: String, password: String): AuthResult

    /** Register new user with email + password, then update display name. */
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): AuthResult

    /** Complete Google sign-in using the account returned from the chooser intent. */
    suspend fun signInWithGoogle(account: GoogleSignInAccount): AuthResult

    /** Send password-reset email. Returns Success with a placeholder user on success. */
    suspend fun sendPasswordReset(email: String): AuthResult

    /** Sign out of Firebase and Google. */
    suspend fun signOut()

    /** Re-send email verification to the currently signed-in user. */
    suspend fun sendEmailVerification(): AuthResult
}
