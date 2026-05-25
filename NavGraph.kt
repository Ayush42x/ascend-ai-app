package com.ascendai.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.ascendai.viewmodel.AuthViewModel

// ─── Route constants ──────────────────────────────────────────────────────────

object Screen {
    const val SPLASH          = "splash"
    const val ONBOARDING      = "onboarding"
    const val LOGIN           = "login"
    const val SIGN_UP         = "sign_up"
    const val FORGOT_PASSWORD = "forgot_password"
    const val DASHBOARD       = "dashboard"
    const val GOAL_CREATION   = "goal_creation"
    const val DAILY_TASKS     = "daily_tasks"
    const val AI_CHAT         = "ai_chat"
    const val ANALYTICS       = "analytics"
    const val PROFILE         = "profile"
    const val SETTINGS        = "settings"
    const val CALENDAR        = "calendar"
}

// ─── Root nav graph ───────────────────────────────────────────────────────────

@Composable
fun AscendNavGraph(
    startDestination: String = Screen.SPLASH
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val sessionUser by authViewModel.sessionUser.collectAsState()

    NavHost(
        navController    = navController,
        startDestination = startDestination,
        enterTransition  = { fadeIn(tween(300)) + slideInHorizontally { it / 8 } },
        exitTransition   = { fadeOut(tween(200)) },
        popEnterTransition  = { fadeIn(tween(300)) },
        popExitTransition   = { fadeOut(tween(200)) + slideOutHorizontally { it / 8 } }
    ) {

        // ── Splash ─────────────────────────────────────────────────────────
        composable(Screen.SPLASH) {
            SplashScreen(
                onNavigateNext = {
                    if (sessionUser != null) {
                        navController.navigate(Screen.DASHBOARD) {
                            popUpTo(Screen.SPLASH) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.LOGIN) {
                            popUpTo(Screen.SPLASH) { inclusive = true }
                        }
                    }
                }
            )
        }

        // ── Auth group ─────────────────────────────────────────────────────
        navigation(startDestination = Screen.LOGIN, route = "auth") {

            composable(Screen.LOGIN) {
                LoginScreen(
                    onNavigateToSignUp = {
                        navController.navigate(Screen.SIGN_UP)
                    },
                    onNavigateToDashboard = {
                        navController.navigate(Screen.DASHBOARD) {
                            popUpTo("auth") { inclusive = true }
                        }
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.FORGOT_PASSWORD)
                    }
                )
            }

            composable(Screen.SIGN_UP) {
                SignUpScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onNavigateToDashboard = {
                        navController.navigate(Screen.DASHBOARD) {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.FORGOT_PASSWORD) {
                ForgotPasswordScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // ── App group ──────────────────────────────────────────────────────
        navigation(startDestination = Screen.DASHBOARD, route = "app") {

            composable(Screen.DASHBOARD) {
                // Placeholder — replace with real DashboardScreen
                DashboardPlaceholder(
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate("auth") {
                            popUpTo("app") { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.GOAL_CREATION) {
                // GoalCreationScreen(...)
            }

            composable(Screen.AI_CHAT) {
                // AIChatScreen(...)
            }

            composable(Screen.ANALYTICS) {
                // AnalyticsScreen(...)
            }

            composable(Screen.PROFILE) {
                // ProfileScreen(...)
            }

            composable(Screen.SETTINGS) {
                // SettingsScreen(...)
            }
        }
    }
}

// ─── Placeholder dashboard (replace with real screen later) ──────────────────

@Composable
private fun DashboardPlaceholder(onSignOut: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        contentAlignment = androidx.compose.ui.Alignment.Center,
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(com.ascendai.ui.theme.BackgroundDark)
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text(
                "Dashboard",
                color = com.ascendai.ui.theme.TextPrimary,
                style = androidx.compose.ui.text.TextStyle(
                    fontSize   = 24.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
            androidx.compose.foundation.layout.Spacer(
                androidx.compose.ui.Modifier.height(24.dp)
            )
            AscendOutlinedButton(text = "Sign out", onClick = onSignOut)
        }
    }
}
