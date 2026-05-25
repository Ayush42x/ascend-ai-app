package com.ascendai.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ascendai.ui.components.*
import com.ascendai.ui.theme.*
import com.ascendai.viewmodel.AuthEvent
import com.ascendai.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.loginState.collectAsState()
    val context = LocalContext.current

    // ── Collect one-shot events ────────────────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AuthEvent.NavigateToDashboard -> onNavigateToDashboard()
                else -> Unit
            }
        }
    }

    // ── Google sign-in launcher ────────────────────────────────────────────
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID") // replace with your Firebase web client ID
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    .getResult(ApiException::class.java)
                viewModel.onGoogleSignIn(account)
            } catch (e: ApiException) {
                // State update handled in ViewModel
            }
        }
    }

    // ── UI ─────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Subtle radial glow behind header
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(320.dp)
                .offset(y = (-80).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PrimaryViolet.copy(alpha = 0.15f), Color.Transparent)
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
            Spacer(Modifier.height(80.dp))

            // ── Logo / wordmark ──────────────────────────────────────────
            AscendLogo()

            Spacer(Modifier.height(48.dp))

            // ── Headline ─────────────────────────────────────────────────
            Text(
                text  = "Welcome back",
                style = TextStyle(
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text  = "Sign in to continue your journey",
                style = TextStyle(fontSize = 15.sp, color = TextSecondary)
            )

            Spacer(Modifier.height(36.dp))

            // ── Error banner ─────────────────────────────────────────────
            if (!state.generalError.isNullOrBlank()) {
                ErrorBanner(message = state.generalError!!)
                Spacer(Modifier.height(16.dp))
            }

            // ── Fields ───────────────────────────────────────────────────
            AscendTextField(
                value            = state.email,
                onValueChange    = viewModel::onLoginEmailChange,
                label            = "Email",
                leadingIcon      = Icons.Outlined.Email,
                error            = state.emailError,
                keyboardOptions  = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next
                )
            )

            Spacer(Modifier.height(14.dp))

            AscendTextField(
                value                      = state.password,
                onValueChange              = viewModel::onLoginPasswordChange,
                label                      = "Password",
                leadingIcon                = Icons.Outlined.Lock,
                isPassword                 = true,
                passwordVisible            = state.isPasswordVisible,
                onPasswordVisibilityToggle = viewModel::onLoginPasswordVisibilityToggle,
                error                      = state.passwordError,
                keyboardOptions            = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done
                )
            )

            // ── Forgot password ──────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = onNavigateToForgotPassword) {
                    Text(
                        text  = "Forgot password?",
                        style = TextStyle(
                            fontSize   = 13.sp,
                            color      = PrimaryVioletLight,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Sign in button ───────────────────────────────────────────
            AscendPrimaryButton(
                text      = "Sign in",
                onClick   = viewModel::onLoginSubmit,
                isLoading = state.isLoading
            )

            Spacer(Modifier.height(24.dp))

            DividerWithText("or continue with")

            Spacer(Modifier.height(24.dp))

            // ── Google button ────────────────────────────────────────────
            GoogleSignInButton(
                onClick   = { googleLauncher.launch(googleSignInClient.signInIntent) },
                isLoading = false
            )

            Spacer(Modifier.height(40.dp))

            // ── Sign up link ─────────────────────────────────────────────
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = TextSecondary, fontSize = 14.sp)) {
                        append("Don't have an account? ")
                    }
                    withStyle(
                        SpanStyle(
                            color      = PrimaryViolet,
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 14.sp
                        )
                    ) { append("Sign up") }
                },
                modifier = Modifier.clickable { onNavigateToSignUp() }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Ascend logo mark ─────────────────────────────────────────────────────────

@Composable
private fun AscendLogo() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(listOf(PrimaryViolet, PrimaryVioletDark))
                )
        ) {
            Text("A", style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White))
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text  = "Ascend",
            style = TextStyle(
                fontSize      = 24.sp,
                fontWeight    = FontWeight.Bold,
                color         = TextPrimary,
                letterSpacing = (-0.5).sp
            )
        )
    }
}

// ─── Google sign-in button ────────────────────────────────────────────────────

@Composable
fun GoogleSignInButton(onClick: () -> Unit, isLoading: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(0.5.dp, BorderDark, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = TextSecondary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Google "G" icon drawn with colored text — replace with real asset in production
                Text(
                    text  = "G",
                    style = TextStyle(
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = GoogleBlue
                    )
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text  = "Continue with Google",
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                )
            }
        }
    }
}
