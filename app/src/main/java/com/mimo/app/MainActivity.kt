package com.mimo.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mimo.app.data.local.AppPreferences
import com.mimo.app.data.network.AuthResult
import com.mimo.app.data.network.GoogleDriveService
import com.mimo.app.data.network.SupabaseAuthService
import com.mimo.app.data.network.SupabaseSessionManager
import com.mimo.app.ui.screens.HomeScreen
import com.mimo.app.ui.screens.LoginScreen
import com.mimo.app.ui.screens.OtpVerificationScreen
import com.mimo.app.ui.screens.SignUpScreen
import com.mimo.app.ui.screens.WelcomeScreen
import com.mimo.app.ui.theme.AppWhite
import com.mimo.app.ui.theme.MimoTheme
import kotlinx.coroutines.launch

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

        // Initialize app preferences, Supabase Auth, Google Drive service, and SaveRepository
        AppPreferences.initialize(applicationContext)
        SupabaseAuthService.initialize(applicationContext)
        GoogleDriveService.initialize(applicationContext)
        com.mimo.app.data.repository.SaveRepository.initialize(applicationContext)

        intent?.data?.let { uri ->
            GoogleDriveService.handleAuthCallback(uri)
            if (SupabaseAuthService.handleAuthCallback(uri)) {
                AppPreferences.setHasSeenWelcome(true)
            }
        }

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { uri ->
            GoogleDriveService.handleAuthCallback(uri)
            if (SupabaseAuthService.handleAuthCallback(uri)) {
                AppPreferences.setHasSeenWelcome(true)
            }
        }
    }
}

private fun openBrowser(context: Context, url: String) {
    try {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Persistent start destination:
    // 1. Authenticated user -> straight to HOME
    // 2. Previously viewed welcome screen -> LOGIN
    // 3. First install -> WELCOME
    val initialScreen = when {
        SupabaseSessionManager.isAuthenticated() -> Screen.HOME
        AppPreferences.hasSeenWelcome() -> Screen.LOGIN
        else -> Screen.WELCOME
    }

    var currentScreen by remember { mutableStateOf(initialScreen) }
    var isForwardNav by remember { mutableStateOf(true) }

    fun navigateTo(target: Screen, forward: Boolean = true) {
        SupabaseAuthService.clearError()
        isForwardNav = forward
        currentScreen = target
    }

    // Auto-navigate to HOME when session state becomes authenticated
    LaunchedEffect(SupabaseSessionManager.isAuthenticatedState) {
        if (SupabaseSessionManager.isAuthenticatedState) {
            com.mimo.app.data.repository.SaveRepository.loadData()
            if (currentScreen != Screen.HOME) {
                AppPreferences.setHasSeenWelcome(true)
                navigateTo(Screen.HOME, forward = true)
            }
        }
    }

    // Android hardware back button handler
    BackHandler(enabled = currentScreen != Screen.WELCOME && currentScreen != Screen.HOME) {
        when (currentScreen) {
            Screen.LOGIN -> {
                if (AppPreferences.hasSeenWelcome()) {
                    (context as? Activity)?.finish()
                } else {
                    navigateTo(Screen.WELCOME, forward = false)
                }
            }
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
                        AppPreferences.setHasSeenWelcome(true)
                        navigateTo(Screen.LOGIN, forward = true)
                    }
                )
            }

            Screen.LOGIN -> {
                LoginScreen(
                    onSignInClick = { email, password ->
                        coroutineScope.launch {
                            val result = SupabaseAuthService.signInWithEmail(email, password)
                            if (result is AuthResult.Success) {
                                AppPreferences.setHasSeenWelcome(true)
                                navigateTo(Screen.HOME, forward = true)
                            }
                        }
                    },
                    onSignUpClick = {
                        navigateTo(Screen.SIGN_UP, forward = true)
                    },
                    onForgotPasswordClick = {
                        navigateTo(Screen.OTP_VERIFICATION, forward = true)
                    },
                    onGoogleSignInClick = {
                        openBrowser(context, SupabaseAuthService.getGoogleOAuthUrl())
                    },
                    isLoading = SupabaseAuthService.isLoading,
                    errorMessage = SupabaseAuthService.errorMessage
                )
            }

            Screen.SIGN_UP -> {
                var confirmationMsg by remember { mutableStateOf<String?>(null) }
                SignUpScreen(
                    onSignUpClick = { name, email, password ->
                        coroutineScope.launch {
                            confirmationMsg = null
                            val result = SupabaseAuthService.signUpWithEmail(name, email, password)
                            when (result) {
                                is AuthResult.Success -> {
                                    AppPreferences.setHasSeenWelcome(true)
                                    navigateTo(Screen.HOME, forward = true)
                                }
                                is AuthResult.RequiresEmailConfirmation -> {
                                    confirmationMsg = "Confirmation email sent. Please check your inbox."
                                }
                                is AuthResult.Error -> {}
                            }
                        }
                    },
                    onSignInClick = {
                        navigateTo(Screen.LOGIN, forward = false)
                    },
                    onForgotPasswordClick = {
                        navigateTo(Screen.OTP_VERIFICATION, forward = true)
                    },
                    onGoogleSignUpClick = {
                        openBrowser(context, SupabaseAuthService.getGoogleOAuthUrl())
                    },
                    isLoading = SupabaseAuthService.isLoading,
                    errorMessage = SupabaseAuthService.errorMessage,
                    infoMessage = confirmationMsg
                )
            }

            Screen.OTP_VERIFICATION -> {
                OtpVerificationScreen(
                    onBackClick = {
                        navigateTo(Screen.LOGIN, forward = false)
                    },
                    onContinueClick = { otpCode ->
                        AppPreferences.setHasSeenWelcome(true)
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
                        coroutineScope.launch {
                            com.mimo.app.data.repository.SaveRepository.clearUserData()
                            SupabaseAuthService.signOut()
                            navigateTo(Screen.LOGIN, forward = false)
                        }
                    }
                )
            }
        }
    }
}
