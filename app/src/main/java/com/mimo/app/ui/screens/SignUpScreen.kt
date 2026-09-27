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
fun SignUpScreen(
    onSignUpClick: (String, String, String) -> Unit,
    onSignInClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onGoogleSignUpClick: () -> Unit = {},
    onAppleSignUpClick: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null,
    infoMessage: String? = null
) {
    var name by remember { mutableStateOf("") }
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

        // Top center: app logo
        AppLogo(size = 60)

        Spacer(modifier = Modifier.height(28.dp))

        // Name input
        AppInputField(
            label = "Name",
            value = name,
            onValueChange = { name = it },
            placeholder = "Enter your name",
            keyboardType = KeyboardType.Text
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email input
        AppInputField(
            label = "Email",
            value = email,
            onValueChange = { email = it },
            placeholder = "Enter your email",
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = errorMessage,
                color = AppAccentRed,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(14.dp))
        } else if (!infoMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = infoMessage,
                color = AppBlack,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(14.dp))
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Sign Up Button
        PrimaryPillButton(
            text = "Sign Up",
            onClick = { onSignUpClick(name, email, password) },
            isLoading = isLoading,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(22.dp))

        // OR Divider
        OrDivider()

        Spacer(modifier = Modifier.height(22.dp))

        // Google Button
        SecondarySocialButton(
            text = "Continue with Google",
            onClick = onGoogleSignUpClick,
            isGoogle = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Apple Button
        SecondarySocialButton(
            text = "Continue with Apple",
            onClick = onAppleSignUpClick,
            isGoogle = false
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Bottom text: Don't have an account? Sign in
        Text(
            text = buildAnnotatedString {
                append("Already have an account? ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = AppBlack)) {
                    append("Sign in")
                }
            },
            fontSize = 14.sp,
            color = AppLightGrey,
            modifier = Modifier
                .clickable(onClick = onSignInClick)
                .padding(bottom = 24.dp)
        )
    }
}
