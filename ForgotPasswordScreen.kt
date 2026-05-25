package com.ascendai.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ascendai.ui.components.*
import com.ascendai.ui.theme.*
import com.ascendai.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.forgotState.collectAsState()

    // Clean up state when leaving
    DisposableEffect(Unit) {
        onDispose { viewModel.resetForgotPasswordState() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Ambient glow
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(260.dp)
                .offset(x = (-60).dp, y = (-60).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PrimaryVioletDark.copy(alpha = 0.18f), Color.Transparent)
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

            // Back button
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick  = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint               = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Content switches between form / success ────────────────────
            AnimatedContent(
                targetState = state.isSuccess,
                transitionSpec = {
                    fadeIn(initialAlpha = 0f) + slideInVertically { it / 4 } togetherWith
                    fadeOut()
                },
                label = "forgot_content"
            ) { isSuccess ->
                if (isSuccess) {
                    SuccessState(email = state.email, onBackToLogin = onNavigateBack)
                } else {
                    FormState(
                        email        = state.email,
                        emailError   = state.emailError,
                        generalError = state.generalError,
                        isLoading    = state.isLoading,
                        onEmailChange = viewModel::onForgotEmailChange,
                        onSubmit     = viewModel::onForgotSubmit,
                        onBack       = onNavigateBack
                    )
                }
            }
        }
    }
}

@Composable
private fun FormState(
    email: String,
    emailError: String?,
    generalError: String?,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        listOf(PrimaryViolet.copy(alpha = 0.2f), PrimaryVioletDark.copy(alpha = 0.1f))
                    )
                )
                .border(0.5.dp, PrimaryViolet.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
        ) {
            Icon(
                imageVector        = Icons.Outlined.Email,
                contentDescription = null,
                tint               = PrimaryViolet,
                modifier           = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text  = "Reset password",
            style = TextStyle(
                fontSize      = 26.sp,
                fontWeight    = FontWeight.Bold,
                color         = TextPrimary,
                letterSpacing = (-0.4).sp
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text  = "Enter your email address and we'll send you a link to reset your password.",
            style = TextStyle(
                fontSize   = 14.sp,
                color      = TextSecondary,
                lineHeight = 22.sp
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        if (!generalError.isNullOrBlank()) {
            ErrorBanner(message = generalError)
            Spacer(Modifier.height(16.dp))
        }

        AscendTextField(
            value           = email,
            onValueChange   = onEmailChange,
            label           = "Email address",
            leadingIcon     = Icons.Outlined.Email,
            error           = emailError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction    = ImeAction.Done
            )
        )

        Spacer(Modifier.height(28.dp))

        AscendPrimaryButton(
            text      = "Send reset link",
            onClick   = onSubmit,
            isLoading = isLoading
        )

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onBack) {
            Text(
                text  = "Back to sign in",
                style = TextStyle(fontSize = 14.sp, color = TextSecondary)
            )
        }
    }
}

@Composable
private fun SuccessState(email: String, onBackToLogin: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success icon with animated glow border
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SuccessGreen.copy(alpha = 0.1f))
                .border(0.5.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
        ) {
            Icon(
                imageVector        = Icons.Outlined.MarkEmailRead,
                contentDescription = null,
                tint               = SuccessGreen,
                modifier           = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text  = "Check your inbox",
            style = TextStyle(
                fontSize      = 26.sp,
                fontWeight    = FontWeight.Bold,
                color         = TextPrimary,
                letterSpacing = (-0.4).sp
            )
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text  = "We've sent a password reset link to",
            style = TextStyle(fontSize = 14.sp, color = TextSecondary)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text  = email,
            style = TextStyle(
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color      = PrimaryVioletLight
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text  = "The link will expire in 24 hours. Check your spam folder if you don't see it.",
            style = TextStyle(
                fontSize   = 13.sp,
                color      = TextTertiary,
                lineHeight = 20.sp
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(40.dp))

        AscendPrimaryButton(
            text    = "Back to sign in",
            onClick = onBackToLogin
        )
    }
}
