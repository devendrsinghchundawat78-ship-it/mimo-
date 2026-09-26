package com.mimo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mimo.app.ui.screens.LoginScreen
import com.mimo.app.ui.screens.OtpVerificationScreen
import com.mimo.app.ui.screens.SignUpScreen
import com.mimo.app.ui.theme.AppWhite
import com.mimo.app.ui.theme.MimoTheme

enum class AuthScreen {
    LOGIN,
    SIGN_UP,
    OTP_VERIFICATION
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MimoTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    color = AppWhite
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf(AuthScreen.LOGIN) }

    when (currentScreen) {
        AuthScreen.LOGIN -> {
            LoginScreen(
                onSignInClick = { email, password ->
                    // Navigate to OTP Verification upon sign in
                    currentScreen = AuthScreen.OTP_VERIFICATION
                },
                onSignUpClick = {
                    currentScreen = AuthScreen.SIGN_UP
                },
                onForgotPasswordClick = {
                    currentScreen = AuthScreen.OTP_VERIFICATION
                }
            )
        }

        AuthScreen.SIGN_UP -> {
            SignUpScreen(
                onSignUpClick = { name, email, password ->
                    // Navigate to OTP Verification upon sign up
                    currentScreen = AuthScreen.OTP_VERIFICATION
                },
                onSignInClick = {
                    currentScreen = AuthScreen.LOGIN
                },
                onForgotPasswordClick = {
                    currentScreen = AuthScreen.OTP_VERIFICATION
                }
            )
        }

        AuthScreen.OTP_VERIFICATION -> {
            OtpVerificationScreen(
                onBackClick = {
                    currentScreen = AuthScreen.LOGIN
                },
                onContinueClick = { otpCode ->
                    // Verified action
                },
                onResendOtpClick = {
                    // Resend action
                }
            )
        }
    }
}
