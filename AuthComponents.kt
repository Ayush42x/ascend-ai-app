package com.ascendai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ascendai.ui.theme.*

// ─── Primary gradient button ──────────────────────────────────────────────────

@Composable
fun AscendPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(PrimaryViolet, PrimaryVioletDark)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled && !isLoading) gradientBrush else Brush.horizontalGradient(listOf(BorderDark, BorderDark)))
            .clickable(
                enabled = enabled && !isLoading,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color     = TextPrimary,
                modifier  = Modifier.size(22.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text  = text,
                style = TextStyle(
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (enabled) Color.White else TextTertiary
                )
            )
        }
    }
}

// ─── Outlined secondary button ────────────────────────────────────────────────

@Composable
fun AscendOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: Painter? = null,
    isLoading: Boolean = false
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(0.5.dp, BorderDark, RoundedCornerShape(16.dp))
            .clickable(
                enabled = !isLoading,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = TextSecondary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (leadingIcon != null) {
                    Icon(
                        painter          = leadingIcon,
                        contentDescription = null,
                        tint             = Color.Unspecified,
                        modifier         = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    text  = text,
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                )
            }
        }
    }
}

// ─── Auth text field ──────────────────────────────────────────────────────────

@Composable
fun AscendTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityToggle: (() -> Unit)? = null,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value            = value,
            onValueChange    = onValueChange,
            label            = {
                Text(
                    label,
                    style = TextStyle(fontSize = 14.sp, color = if (error != null) ErrorRed else TextSecondary)
                )
            },
            leadingIcon      = leadingIcon?.let {
                {
                    Icon(
                        imageVector   = it,
                        contentDescription = null,
                        tint          = if (error != null) ErrorRed else TextSecondary,
                        modifier      = Modifier.size(20.dp)
                    )
                }
            },
            trailingIcon     = if (isPassword) {
                {
                    IconButton(onClick = { onPasswordVisibilityToggle?.invoke() }) {
                        Icon(
                            imageVector   = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint          = TextSecondary
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            isError          = error != null,
            keyboardOptions  = keyboardOptions,
            keyboardActions  = keyboardActions,
            enabled          = enabled,
            singleLine       = true,
            shape            = RoundedCornerShape(14.dp),
            colors           = OutlinedTextFieldDefaults.colors(
                focusedTextColor        = TextPrimary,
                unfocusedTextColor      = TextPrimary,
                focusedBorderColor      = PrimaryViolet,
                unfocusedBorderColor    = BorderDark,
                errorBorderColor        = ErrorRed,
                cursorColor             = PrimaryViolet,
                focusedLabelColor       = PrimaryViolet,
                unfocusedLabelColor     = TextSecondary,
                errorLabelColor         = ErrorRed,
                focusedContainerColor   = CardDark,
                unfocusedContainerColor = CardDark,
                errorContainerColor     = Color(0xFF1A0F0F)
            ),
            textStyle = TextStyle(fontSize = 15.sp),
            modifier  = Modifier.fillMaxWidth()
        )

        // Error message
        AnimatedVisibility(
            visible = error != null,
            enter   = fadeIn() + slideInVertically(),
            exit    = fadeOut()
        ) {
            if (error != null) {
                Text(
                    text     = error,
                    color    = ErrorRed,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
        }
    }
}

// ─── Divider with centered text ───────────────────────────────────────────────

@Composable
fun DividerWithText(text: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            thickness = 0.5.dp,
            color     = BorderDark
        )
        Text(
            text     = text,
            color    = TextTertiary,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            thickness = 0.5.dp,
            color     = BorderDark
        )
    }
}

// ─── Error banner ─────────────────────────────────────────────────────────────

@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = message.isNotBlank(),
        enter   = fadeIn() + slideInVertically(),
        exit    = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2A0F0F))
                .border(0.5.dp, ErrorRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(text = message, color = ErrorRed, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

// ─── Password strength indicator ──────────────────────────────────────────────

@Composable
fun PasswordStrengthBar(password: String, modifier: Modifier = Modifier) {
    val strength = when {
        password.length >= 10 &&
        password.any { it.isDigit() } &&
        password.any { it.isUpperCase() } &&
        password.any { it.isLowerCase() } &&
        password.any { "!@#\$%^&*".contains(it) } -> 3 // strong
        password.length >= 8 &&
        password.any { it.isDigit() } &&
        password.any { it.isUpperCase() } -> 2           // medium
        password.length >= 6 -> 1                         // weak
        else -> 0
    }

    val (color, label) = when (strength) {
        3    -> Pair(SuccessGreen, "Strong")
        2    -> Pair(WarningAmber, "Medium")
        1    -> Pair(ErrorRed, "Weak")
        else -> Pair(BorderDark, "")
    }

    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (index < strength) color else BorderDark)
                )
            }
        }
        if (strength > 0) {
            Spacer(Modifier.height(4.dp))
            Text(label, color = color, fontSize = 11.sp)
        }
    }
}
