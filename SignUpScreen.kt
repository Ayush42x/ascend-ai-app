package com.ascendai.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ascendai.ui.components.*
import com.ascendai.ui.theme.*
import com.ascendai.viewmodel.AuthEvent
import com.ascendai.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.signUpState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Collect one-shot events ────────────────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AuthEvent.NavigateToDashboard -> onNavigateToDashboard()
                is AuthEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost   = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Glow effect
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(280.dp)
                    .offset(x = 80.dp, y = (-60).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PrimaryVioletDark.copy(alpha = 0.2f), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(56.dp))

                // ── Back button ──────────────────────────────────────────
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick  = onNavigateToLogin,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint               = TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Headline ─────────────────────────────────────────────
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text  = "Create account",
                            style = TextStyle(
                                fontSize      = 28.sp,
                                fontWeight    = FontWeight.Bold,
                                color         = TextPrimary,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text  = "Start your ascent today",
                            style = TextStyle(fontSize = 15.sp, color = TextSecondary)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ── Error banner ─────────────────────────────────────────
                if (!state.generalError.isNullOrBlank()) {
                    ErrorBanner(message = state.generalError!!)
                    Spacer(Modifier.height(16.dp))
                }

                // ── Full name ─────────────────────────────────────────────
                AscendTextField(
                    value           = state.name,
                    onValueChange   = viewModel::onSignUpNameChange,
                    label           = "Full name",
                    leadingIcon     = Icons.Outlined.Person,
                    error           = state.nameError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction      = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(14.dp))

                // ── Email ─────────────────────────────────────────────────
                AscendTextField(
                    value           = state.email,
                    onValueChange   = viewModel::onSignUpEmailChange,
                    label           = "Email",
                    leadingIcon     = Icons.Outlined.Email,
                    error           = state.emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction    = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(14.dp))

                // ── Password ──────────────────────────────────────────────
                AscendTextField(
                    value                      = state.password,
                    onValueChange              = viewModel::onSignUpPasswordChange,
                    label                      = "Password",
                    leadingIcon                = Icons.Outlined.Lock,
                    isPassword                 = true,
                    passwordVisible            = state.isPasswordVisible,
                    onPasswordVisibilityToggle = viewModel::onSignUpPasswordVisibilityToggle,
                    error                      = state.passwordError,
                    keyboardOptions            = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Next
                    )
                )

                // Password strength bar
                AnimatedVisibility(visible = state.password.isNotEmpty()) {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        PasswordStrengthBar(password = state.password, modifier = Modifier.fillMaxWidth())
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ── Confirm password ──────────────────────────────────────
                AscendTextField(
                    value                      = state.confirmPassword,
                    onValueChange              = viewModel::onSignUpConfirmPasswordChange,
                    label                      = "Confirm password",
                    leadingIcon                = Icons.Outlined.Lock,
                    isPassword                 = true,
                    passwordVisible            = state.isConfirmPasswordVisible,
                    onPasswordVisibilityToggle = viewModel::onSignUpConfirmPasswordVisibilityToggle,
                    error                      = state.confirmPasswordError,
                    keyboardOptions            = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Done
                    )
                )

                Spacer(Modifier.height(28.dp))

                // ── Terms notice ──────────────────────────────────────────
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = TextTertiary, fontSize = 12.sp)) {
                            append("By creating an account you agree to our ")
                        }
                        withStyle(SpanStyle(color = PrimaryVioletLight, fontSize = 12.sp)) {
                            append("Terms of Service")
                        }
                        withStyle(SpanStyle(color = TextTertiary, fontSize = 12.sp)) {
                            append(" and ")
                        }
                        withStyle(SpanStyle(color = PrimaryVioletLight, fontSize = 12.sp)) {
                            append("Privacy Policy")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                // ── Submit ────────────────────────────────────────────────
                AscendPrimaryButton(
                    text      = "Create account",
                    onClick   = viewModel::onSignUpSubmit,
                    isLoading = state.isLoading
                )

                Spacer(Modifier.height(28.dp))

                // ── Login link ────────────────────────────────────────────
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = TextSecondary, fontSize = 14.sp)) {
                            append("Already have an account? ")
                        }
                        withStyle(
                            SpanStyle(
                                color      = PrimaryViolet,
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 14.sp
                            )
                        ) { append("Sign in") }
                    },
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
