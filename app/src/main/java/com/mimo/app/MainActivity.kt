package com.mimo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mimo.app.ui.screens.HomeScreen
import com.mimo.app.ui.screens.LoginScreen
import com.mimo.app.ui.screens.OtpVerificationScreen
import com.mimo.app.ui.screens.SignUpScreen
import com.mimo.app.ui.screens.WelcomeScreen
import com.mimo.app.ui.theme.AppWhite
import com.mimo.app.ui.theme.MimoTheme

enum class Screen {
    WELCOME,
    LOGIN,
    SIGN_UP,
    OTP_VERIFICATION,
    HOME
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
    var currentScreen by remember { mutableStateOf(Screen.WELCOME) }
    var isForwardNav by remember { mutableStateOf(true) }

    fun navigateTo(target: Screen, forward: Boolean = true) {
        isForwardNav = forward
        currentScreen = target
    }

    // Android hardware back button handler
    BackHandler(enabled = currentScreen != Screen.WELCOME && currentScreen != Screen.HOME) {
        when (currentScreen) {
            Screen.LOGIN -> navigateTo(Screen.WELCOME, forward = false)
            Screen.SIGN_UP -> navigateTo(Screen.LOGIN, forward = false)
            Screen.OTP_VERIFICATION -> navigateTo(Screen.LOGIN, forward = false)
            else -> {}
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (isForwardNav) {
                (slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth / 3 },
                    animationSpec = tween(durationMillis = 350)
                ) + fadeIn(animationSpec = tween(durationMillis = 350)))
                    .togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth / 3 },
                            animationSpec = tween(durationMillis = 350)
                        ) + fadeOut(animationSpec = tween(durationMillis = 250))
                    )
            } else {
                (slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth / 3 },
                    animationSpec = tween(durationMillis = 350)
                ) + fadeIn(animationSpec = tween(durationMillis = 350)))
                    .togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth / 3 },
                            animationSpec = tween(durationMillis = 350)
                        ) + fadeOut(animationSpec = tween(durationMillis = 250))
                    )
            }
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            Screen.WELCOME -> {
                WelcomeScreen(
                    onContinueClick = {
                        navigateTo(Screen.LOGIN, forward = true)
                    }
                )
            }

            Screen.LOGIN -> {
                LoginScreen(
                    onSignInClick = { _, _ ->
                        // Direct seamless login without blocking
                        navigateTo(Screen.HOME, forward = true)
                    },
                    onSignUpClick = {
                        navigateTo(Screen.SIGN_UP, forward = true)
                    },
                    onForgotPasswordClick = {
                        navigateTo(Screen.OTP_VERIFICATION, forward = true)
                    },
                    onGoogleSignInClick = {
                        navigateTo(Screen.HOME, forward = true)
                    },
                    onAppleSignInClick = {
                        navigateTo(Screen.HOME, forward = true)
                    }
                )
            }

            Screen.SIGN_UP -> {
                SignUpScreen(
                    onSignUpClick = { _, _, _ ->
                        // Direct seamless sign up without blocking
                        navigateTo(Screen.HOME, forward = true)
                    },
                    onSignInClick = {
                        navigateTo(Screen.LOGIN, forward = false)
                    },
                    onForgotPasswordClick = {
                        navigateTo(Screen.OTP_VERIFICATION, forward = true)
                    },
                    onGoogleSignUpClick = {
                        navigateTo(Screen.HOME, forward = true)
                    },
                    onAppleSignUpClick = {
                        navigateTo(Screen.HOME, forward = true)
                    }
                )
            }

            Screen.OTP_VERIFICATION -> {
                OtpVerificationScreen(
                    onBackClick = {
                        navigateTo(Screen.LOGIN, forward = false)
                    },
                    onContinueClick = { otpCode ->
                        navigateTo(Screen.HOME, forward = true)
                    },
                    onResendOtpClick = {
                        // Resend logic
                    }
                )
            }

            Screen.HOME -> {
                HomeScreen(
                    onProfileClick = {
                        // Profile destination
                    },
                    onSignOut = {
                        navigateTo(Screen.WELCOME, forward = false)
                    }
                )
            }
        }
    }
}
