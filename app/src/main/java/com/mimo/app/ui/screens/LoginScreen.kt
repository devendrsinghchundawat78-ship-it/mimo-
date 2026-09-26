package com.mimo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimo.app.ui.components.AppInputField
import com.mimo.app.ui.components.AppLogo
import com.mimo.app.ui.components.OrDivider
import com.mimo.app.ui.components.PrimaryPillButton
import com.mimo.app.ui.components.SecondarySocialButton
import com.mimo.app.ui.theme.AppAccentRed
import com.mimo.app.ui.theme.AppBlack
import com.mimo.app.ui.theme.AppBorderGrey
import com.mimo.app.ui.theme.AppLightGrey
import com.mimo.app.ui.theme.AppWhite

@Composable
fun LoginScreen(
    onSignInClick: (String, String) -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onGoogleSignInClick: () -> Unit = {},
    onAppleSignInClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppWhite)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Top center: app logo (~60dp)
        AppLogo(size = 60)

        Spacer(modifier = Modifier.height(32.dp))

        // Email input
        AppInputField(
            label = "Email",
            value = email,
            onValueChange = { email = it },
            placeholder = "Enter your email",
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Password input
        AppInputField(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            placeholder = "Enter your password",
            isPassword = true,
            isPasswordVisible = isPasswordVisible,
            onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
            keyboardType = KeyboardType.Password
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Row: Remember me checkbox + Forgot password link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { rememberMe = !rememberMe }
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AppBlack,
                        uncheckedColor = AppBorderGrey,
                        checkmarkColor = AppWhite
                    ),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Remember me",
                    fontSize = 13.sp,
                    color = AppBlack,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Text(
                text = "Forgot password?",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AppAccentRed,
                modifier = Modifier.clickable(onClick = onForgotPasswordClick)
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Sign In Button
        PrimaryPillButton(
            text = "Sign In",
            onClick = { onSignInClick(email, password) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // OR Divider
        OrDivider()

        Spacer(modifier = Modifier.height(24.dp))

        // Google Button
        SecondarySocialButton(
            text = "Continue with Google",
            onClick = onGoogleSignInClick,
            isGoogle = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Apple Button
        SecondarySocialButton(
            text = "Continue with Apple",
            onClick = onAppleSignInClick,
            isGoogle = false
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Bottom text: Don't have an account? Sign up
        Text(
            text = buildAnnotatedString {
                append("Don't have an account? ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = AppBlack)) {
                    append("Sign up")
                }
            },
            fontSize = 14.sp,
            color = AppLightGrey,
            modifier = Modifier
                .clickable(onClick = onSignUpClick)
                .padding(bottom = 24.dp)
        )
    }
}
