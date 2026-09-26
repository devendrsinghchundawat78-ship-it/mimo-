package com.mimo.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimo.app.R
import com.mimo.app.ui.theme.AppBlack
import com.mimo.app.ui.theme.AppBorderGrey
import com.mimo.app.ui.theme.AppInputBg
import com.mimo.app.ui.theme.AppLightGrey
import com.mimo.app.ui.theme.AppWhite

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Int = 60
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(AppInputBg),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_mimo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AppInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppBlack,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(AppInputBg, shape = RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = AppLightGrey,
                            fontSize = 15.sp
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        cursorBrush = SolidColor(AppBlack),
                        textStyle = TextStyle(
                            color = AppBlack,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.SansSerif
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                        visualTransformation = if (isPassword && !isPasswordVisible) {
                            PasswordVisualTransformation()
                        } else {
                            VisualTransformation.None
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (isPassword && onTogglePasswordVisibility != null) {
                    IconButton(
                        onClick = onTogglePasswordVisibility,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = AppLightGrey,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrimaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(AppBlack)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = AppWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SecondarySocialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGoogle: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(AppWhite)
            .border(1.dp, AppBorderGrey, RoundedCornerShape(50.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Minimal Vector Icon representation
            Text(
                text = if (isGoogle) "G" else "",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AppBlack
            )
            if (isGoogle) {
                Spacer(modifier = Modifier.width(10.dp))
            } else {
                // Apple Icon representation
                Text(
                    text = "",
                    fontSize = 16.sp,
                    color = AppBlack
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = AppBlack,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun OrDivider(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = AppBorderGrey
        )
        Text(
            text = "OR",
            color = AppLightGrey,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = AppBorderGrey
        )
    }
}
