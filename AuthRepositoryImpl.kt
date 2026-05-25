package com.ascendai.data.repository

import com.ascendai.domain.model.AscendUser
import com.ascendai.domain.model.AuthResult
import com.ascendai.domain.model.toAscendUser
import com.ascendai.domain.repository.IAuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : IAuthRepository {

    // ─── Current user as a cold Flow ─────────────────────────────────────────

    override val currentUserFlow: Flow<AscendUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toAscendUser())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override val currentUser: AscendUser?
        get() = auth.currentUser?.toAscendUser()

    // ─── Email / password sign-in ─────────────────────────────────────────────

    override suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.Error("Sign in failed — no user returned")
            AuthResult.Success(user.toAscendUser())
        } catch (e: FirebaseAuthException) {
            AuthResult.Error(mapFirebaseError(e.errorCode), e)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "An unexpected error occurred", e)
        }
    }

    // ─── Email / password sign-up ─────────────────────────────────────────────

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return AuthResult.Error("Registration failed — no user returned")

            // Set display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // Create Firestore user document
            createFirestoreUserDocument(firebaseUser.uid, email, displayName)

            // Send email verification
            firebaseUser.sendEmailVerification().await()

            AuthResult.Success(firebaseUser.toAscendUser())
        } catch (e: FirebaseAuthException) {
            AuthResult.Error(mapFirebaseError(e.errorCode), e)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Registration failed", e)
        }
    }

    // ─── Google sign-in ───────────────────────────────────────────────────────

    override suspend fun signInWithGoogle(account: GoogleSignInAccount): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user
                ?: return AuthResult.Error("Google sign-in failed — no user returned")

            // Create Firestore doc if new user
            if (result.additionalUserInfo?.isNewUser == true) {
                createFirestoreUserDocument(
                    uid         = firebaseUser.uid,
                    email       = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "Ascend User"
                )
            }

            AuthResult.Success(firebaseUser.toAscendUser())
        } catch (e: FirebaseAuthException) {
            AuthResult.Error(mapFirebaseError(e.errorCode), e)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Google sign-in failed", e)
        }
    }

    // ─── Password reset ───────────────────────────────────────────────────────

    override suspend fun sendPasswordReset(email: String): AuthResult {
        return try {
            auth.sendPasswordResetEmail(email).await()
            // Return a placeholder success — no real user object needed
            AuthResult.Success(
                AscendUser(
                    uid             = "",
                    email           = email,
                    displayName     = null,
                    photoUrl        = null,
                    isEmailVerified = false,
                    providerId      = "password"
                )
            )
        } catch (e: FirebaseAuthException) {
            AuthResult.Error(mapFirebaseError(e.errorCode), e)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to send reset email", e)
        }
    }

    // ─── Sign out ─────────────────────────────────────────────────────────────

    override suspend fun signOut() {
        auth.signOut()
    }

    // ─── Email verification ───────────────────────────────────────────────────

    override suspend fun sendEmailVerification(): AuthResult {
        return try {
            val user = auth.currentUser
                ?: return AuthResult.Error("No user signed in")
            user.sendEmailVerification().await()
            AuthResult.Success(user.toAscendUser())
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to send verification email", e)
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private suspend fun createFirestoreUserDocument(
        uid: String,
        email: String,
        displayName: String
    ) {
        val userDoc = hashMapOf(
            "uid"         to uid,
            "email"       to email,
            "displayName" to displayName,
            "createdAt"   to com.google.firebase.Timestamp.now(),
            "streak"      to 0,
            "consistency" to 0.0,
            "goals"       to emptyList<String>()
        )
        firestore.collection("users").document(uid).set(userDoc).await()
    }

    /** Maps Firebase error codes to user-friendly messages. */
    private fun mapFirebaseError(errorCode: String): String = when (errorCode) {
        "ERROR_INVALID_EMAIL"               -> "Invalid email address"
        "ERROR_WRONG_PASSWORD"              -> "Incorrect password"
        "ERROR_USER_NOT_FOUND"              -> "No account found with this email"
        "ERROR_USER_DISABLED"               -> "This account has been disabled"
        "ERROR_TOO_MANY_REQUESTS"           -> "Too many attempts. Please try again later"
        "ERROR_OPERATION_NOT_ALLOWED"       -> "This sign-in method is not enabled"
        "ERROR_EMAIL_ALREADY_IN_USE"        -> "An account already exists with this email"
        "ERROR_WEAK_PASSWORD"               -> "Password is too weak"
        "ERROR_NETWORK_REQUEST_FAILED"      -> "No internet connection"
        "ERROR_CREDENTIAL_ALREADY_IN_USE"   -> "This credential is already linked to another account"
        "ERROR_INVALID_CREDENTIAL"          -> "Invalid or expired credential. Please try again"
        else                                -> "Authentication failed. Please try again"
    }
}
