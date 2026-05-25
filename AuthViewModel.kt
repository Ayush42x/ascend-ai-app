package com.ascendai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascendai.domain.model.AscendUser
import com.ascendai.domain.model.AuthResult
import com.ascendai.domain.model.AuthValidator
import com.ascendai.domain.repository.IAuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UI State models ──────────────────────────────────────────────────────────

data class LoginUiState(
    val email: String                = "",
    val password: String             = "",
    val emailError: String?          = null,
    val passwordError: String?       = null,
    val isLoading: Boolean           = false,
    val isPasswordVisible: Boolean   = false,
    val generalError: String?        = null
)

data class SignUpUiState(
    val name: String                 = "",
    val email: String                = "",
    val password: String             = "",
    val confirmPassword: String      = "",
    val nameError: String?           = null,
    val emailError: String?          = null,
    val passwordError: String?       = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean           = false,
    val isPasswordVisible: Boolean   = false,
    val isConfirmPasswordVisible: Boolean = false,
    val generalError: String?        = null
)

data class ForgotPasswordUiState(
    val email: String            = "",
    val emailError: String?      = null,
    val isLoading: Boolean       = false,
    val isSuccess: Boolean       = false,
    val generalError: String?    = null
)

sealed class AuthEvent {
    object NavigateToDashboard : AuthEvent()
    object NavigateToLogin     : AuthEvent()
    data class ShowSnackbar(val message: String) : AuthEvent()
}

// ─── AuthViewModel ────────────────────────────────────────────────────────────

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    // ── Session state (drives nav graph root) ──────────────────────────────
    val sessionUser: StateFlow<AscendUser?> = authRepository.currentUserFlow
        .stateIn(
            scope         = viewModelScope,
            started       = SharingStarted.WhileSubscribed(5_000),
            initialValue  = authRepository.currentUser
        )

    // ── Screen states ──────────────────────────────────────────────────────
    private val _loginState    = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _signUpState   = MutableStateFlow(SignUpUiState())
    val signUpState: StateFlow<SignUpUiState> = _signUpState.asStateFlow()

    private val _forgotState   = MutableStateFlow(ForgotPasswordUiState())
    val forgotState: StateFlow<ForgotPasswordUiState> = _forgotState.asStateFlow()

    // ── One-shot events ────────────────────────────────────────────────────
    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────────────────────────

    fun onLoginEmailChange(value: String) {
        _loginState.update { it.copy(email = value, emailError = null, generalError = null) }
    }

    fun onLoginPasswordChange(value: String) {
        _loginState.update { it.copy(password = value, passwordError = null, generalError = null) }
    }

    fun onLoginPasswordVisibilityToggle() {
        _loginState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginSubmit() {
        val state = _loginState.value
        val emailErr    = AuthValidator.validateEmail(state.email)
        val passwordErr = if (state.password.isBlank()) "Password cannot be empty" else null

        if (emailErr != null || passwordErr != null) {
            _loginState.update { it.copy(emailError = emailErr, passwordError = passwordErr) }
            return
        }

        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true, generalError = null) }
            when (val result = authRepository.signInWithEmail(state.email.trim(), state.password)) {
                is AuthResult.Success -> {
                    _loginState.update { it.copy(isLoading = false) }
                    _events.emit(AuthEvent.NavigateToDashboard)
                }
                is AuthResult.Error -> {
                    _loginState.update { it.copy(isLoading = false, generalError = result.message) }
                }
                is AuthResult.Loading -> Unit
            }
        }
    }

    fun onGoogleSignIn(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true, generalError = null) }
            when (val result = authRepository.signInWithGoogle(account)) {
                is AuthResult.Success -> {
                    _loginState.update { it.copy(isLoading = false) }
                    _events.emit(AuthEvent.NavigateToDashboard)
                }
                is AuthResult.Error -> {
                    _loginState.update { it.copy(isLoading = false, generalError = result.message) }
                }
                is AuthResult.Loading -> Unit
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SIGN UP
    // ─────────────────────────────────────────────────────────────────────────

    fun onSignUpNameChange(value: String) {
        _signUpState.update { it.copy(name = value, nameError = null, generalError = null) }
    }

    fun onSignUpEmailChange(value: String) {
        _signUpState.update { it.copy(email = value, emailError = null, generalError = null) }
    }

    fun onSignUpPasswordChange(value: String) {
        _signUpState.update { it.copy(password = value, passwordError = null, generalError = null) }
    }

    fun onSignUpConfirmPasswordChange(value: String) {
        _signUpState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }
    }

    fun onSignUpPasswordVisibilityToggle() {
        _signUpState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onSignUpConfirmPasswordVisibilityToggle() {
        _signUpState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onSignUpSubmit() {
        val state = _signUpState.value
        val nameErr     = AuthValidator.validateName(state.name)
        val emailErr    = AuthValidator.validateEmail(state.email)
        val passwordErr = AuthValidator.validatePassword(state.password)
        val confirmErr  = AuthValidator.validatePasswordMatch(state.password, state.confirmPassword)

        if (nameErr != null || emailErr != null || passwordErr != null || confirmErr != null) {
            _signUpState.update {
                it.copy(
                    nameError            = nameErr,
                    emailError           = emailErr,
                    passwordError        = passwordErr,
                    confirmPasswordError = confirmErr
                )
            }
            return
        }

        viewModelScope.launch {
            _signUpState.update { it.copy(isLoading = true, generalError = null) }
            when (val result = authRepository.signUpWithEmail(
                email       = state.email.trim(),
                password    = state.password,
                displayName = state.name.trim()
            )) {
                is AuthResult.Success -> {
                    _signUpState.update { it.copy(isLoading = false) }
                    _events.emit(AuthEvent.ShowSnackbar("Account created! Please verify your email."))
                    _events.emit(AuthEvent.NavigateToDashboard)
                }
                is AuthResult.Error -> {
                    _signUpState.update { it.copy(isLoading = false, generalError = result.message) }
                }
                is AuthResult.Loading -> Unit
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FORGOT PASSWORD
    // ─────────────────────────────────────────────────────────────────────────

    fun onForgotEmailChange(value: String) {
        _forgotState.update { it.copy(email = value, emailError = null, generalError = null) }
    }

    fun onForgotSubmit() {
        val state    = _forgotState.value
        val emailErr = AuthValidator.validateEmail(state.email)
        if (emailErr != null) {
            _forgotState.update { it.copy(emailError = emailErr) }
            return
        }

        viewModelScope.launch {
            _forgotState.update { it.copy(isLoading = true, generalError = null) }
            when (val result = authRepository.sendPasswordReset(state.email.trim())) {
                is AuthResult.Success -> {
                    _forgotState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is AuthResult.Error -> {
                    _forgotState.update { it.copy(isLoading = false, generalError = result.message) }
                }
                is AuthResult.Loading -> Unit
            }
        }
    }

    fun resetForgotPasswordState() {
        _forgotState.value = ForgotPasswordUiState()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SIGN OUT
    // ─────────────────────────────────────────────────────────────────────────

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _events.emit(AuthEvent.NavigateToLogin)
        }
    }
}
