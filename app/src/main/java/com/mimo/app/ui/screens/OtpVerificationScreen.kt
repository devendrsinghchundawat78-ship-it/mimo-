package com.mimo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimo.app.ui.components.AppLogo
import com.mimo.app.ui.components.PrimaryPillButton
import com.mimo.app.ui.theme.AppBlack
import com.mimo.app.ui.theme.AppBorderGrey
import com.mimo.app.ui.theme.AppInputBg
import com.mimo.app.ui.theme.AppLightGrey
import com.mimo.app.ui.theme.AppWhite

@Composable
fun OtpVerificationScreen(
    phoneNumber: String = "+XX XXX XXX XXX",
    onBackClick: () -> Unit,
    onContinueClick: (String) -> Unit,
    onResendOtpClick: () -> Unit
) {
    var otpCode by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppWhite)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Top left: circular back arrow button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(AppInputBg)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AppBlack,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Center: small logo or icon (~50dp)
        AppLogo(size = 50)

        Spacer(modifier = Modifier.height(32.dp))

        // Heading "Enter OTP Code"
        Text(
            text = "Enter OTP Code",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppBlack
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle
        Text(
            text = "Check your SMS. We've sent a one-time verification code to $phoneNumber. Enter the code below to verify your account.",
            fontSize = 14.sp,
            color = AppLightGrey,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        // 4 boxes for OTP digits, light grey rounded squares, centered
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Hidden text field capturing input
            BasicTextField(
                value = otpCode,
                onValueChange = {
                    if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                        otpCode = it
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                cursorBrush = SolidColor(AppWhite),
                textStyle = TextStyle(color = AppWhite),
                modifier = Modifier
                    .size(1.dp)
                    .focusRequester(focusRequester)
            )

            // Visible 4 OTP rounded boxes
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val digit = otpCode.getOrNull(i)?.toString() ?: ""
                    val isFocused = otpCode.length == i

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppInputBg)
                            .then(
                                if (isFocused) {
                                    Modifier.border(1.5.dp, AppBlack, RoundedCornerShape(14.dp))
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { focusRequester.requestFocus() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = digit,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppBlack
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Full-width black "Continue" button
        PrimaryPillButton(
            text = "Continue",
            onClick = { onContinueClick(otpCode) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Bottom text: Didn't get OTP? Resend OTP
        Text(
            text = buildAnnotatedString {
                append("Didn't get OTP? ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = AppBlack)) {
                    append("Resend OTP")
                }
            },
            fontSize = 14.sp,
            color = AppLightGrey,
            modifier = Modifier
                .clickable(onClick = onResendOtpClick)
                .padding(bottom = 32.dp)
        )
    }
}
