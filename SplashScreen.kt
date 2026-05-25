package com.ascendai.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ascendai.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateNext: () -> Unit) {

    // ── Animations ────────────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    val logoScale by animateFloatAsState(
        targetValue  = if (visible) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "logo_scale"
    )
    val glowAlpha by animateFloatAsState(
        targetValue   = if (visible) 0.25f else 0f,
        animationSpec = tween(800),
        label = "glow_alpha"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2000)
        onNavigateNext()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Ambient glow
        Box(
            modifier = Modifier
                .size(380.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PrimaryViolet.copy(alpha = glowAlpha), Color.Transparent)
                    )
                )
        )

        // Logo
        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn(tween(600)) + scaleIn(initialScale = 0.8f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .scale(logoScale)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(PrimaryViolet, PrimaryVioletDark)
                            )
                        )
                ) {
                    Text(
                        "A",
                        style = TextStyle(
                            fontSize   = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Ascend",
                    style = TextStyle(
                        fontSize      = 32.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = TextPrimary,
                        letterSpacing = (-1).sp
                    )
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "Achieve anything",
                    style = TextStyle(
                        fontSize = 15.sp,
                        color    = TextTertiary
                    )
                )
            }
        }
    }
}
