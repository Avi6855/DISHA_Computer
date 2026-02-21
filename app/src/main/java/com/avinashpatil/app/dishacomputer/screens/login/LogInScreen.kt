package com.avinashpatil.app.dishacomputer.screens.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.avinashpatil.app.dishacomputer.navigation.Screen


@Composable
fun ModernAnimatedLoginForm(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var isExpanded by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var dishaId by remember { mutableStateOf("") }
    var dishaIdVisible by remember { mutableStateOf(false) }
    
    val loginState by viewModel.loginState.collectAsState()
    val deviceName by viewModel.deviceName.collectAsState()
    
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            // After successful login, navigate to Home and remove Login from back stack
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val boxWidth by animateFloatAsState(
        targetValue = if (isExpanded) 350f else 280f,
        animationSpec = tween(500),
        label = "width"
    )

    val boxHeight by animateFloatAsState(
        targetValue = if (isExpanded) 400f else 120f,
        animationSpec = tween(500),
        label = "height"
    )

    // Theme colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()                          
            .background(backgroundColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(boxWidth.dp)
                .height(boxHeight.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { isExpanded = !isExpanded }
                .drawBehind {
                    rotate(rotation.value) {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    tertiaryColor,
                                    tertiaryColor.copy(alpha = 0.6f),
                                    tertiaryColor.copy(alpha = 0.3f),
                                    Color.Transparent,
                                    Color.Transparent
                                )
                            ),
                            radius = size.maxDimension
                        )
                    }
                    rotate(-rotation.value) {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    primaryColor,
                                    primaryColor.copy(alpha = 0.6f),
                                    primaryColor.copy(alpha = 0.3f),
                                    Color.Transparent,
                                    Color.Transparent
                                )
                            ),
                            radius = size.maxDimension
                        )
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(surfaceColor)
                    .padding(8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isExpanded) 32.dp else 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(surfaceVariantColor.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "⮕ LOGIN ❤",
                        color = tertiaryColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = if (isExpanded) 0.dp else 8.dp)
                    )
                    if (isExpanded) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("Email ID", color = onSurfaceVariantColor) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedTextColor = onSurfaceColor,
                                focusedTextColor = onSurfaceColor,
                                unfocusedContainerColor = surfaceVariantColor.copy(alpha = 0.2f),
                                focusedContainerColor = surfaceVariantColor.copy(alpha = 0.2f),
                                unfocusedBorderColor = outlineColor,
                                focusedBorderColor = primaryColor,
                                cursorColor = primaryColor
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(52.dp)
                        )

                        OutlinedTextField(
                            value = dishaId,
                            onValueChange = { dishaId = it },
                            placeholder = { Text("DISHA ID", color = onSurfaceVariantColor) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedTextColor = onSurfaceColor,
                                focusedTextColor = onSurfaceColor,
                                unfocusedContainerColor = surfaceVariantColor.copy(alpha = 0.2f),
                                focusedContainerColor = surfaceVariantColor.copy(alpha = 0.2f),
                                unfocusedBorderColor = outlineColor,
                                focusedBorderColor = primaryColor,
                                cursorColor = primaryColor
                            ),
                            visualTransformation = if (dishaIdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(
                                    onClick = { dishaIdVisible = !dishaIdVisible }
                                ) {
                                    Icon(
                                        imageVector = if (dishaIdVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = if (dishaIdVisible) "Hide DISHA ID" else "Show DISHA ID",
                                        tint = primaryColor
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(52.dp)
                        )
                        /*
                        OutlinedTextField(
                            value = deviceName,
                            onValueChange = { },
                            placeholder = { Text("Device Name", color = onSurfaceVariantColor) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedTextColor = onSurfaceColor,
                                focusedTextColor = onSurfaceColor,
                                unfocusedContainerColor = surfaceVariantColor.copy(alpha = 0.2f),
                                focusedContainerColor = surfaceVariantColor.copy(alpha = 0.2f),
                                unfocusedBorderColor = outlineColor,
                                focusedBorderColor = primaryColor,
                                cursorColor = primaryColor
                            ),
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(52.dp)
                        )
                         */
                        when (loginState) {
                            is LoginState.Error -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = (loginState as LoginState.Error).message,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            is LoginState.Loading -> {
                                CircularProgressIndicator(
                                    color = primaryColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            else -> {}
                        }

                        Button(
                            onClick = { 
                                if (email.trim().isEmpty() || dishaId.trim().isEmpty()) {
                                    return@Button
                                }
                                viewModel.login(email.trim(), dishaId.trim())
                            },
                            enabled = email.trim().isNotEmpty() && dishaId.trim().isNotEmpty() && loginState !is LoginState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(48.dp)
                        ) {
                            if (loginState is LoginState.Loading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = "LOGIN",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        }
                    }
                }
            }
        }
    }

// Compose Preview
@Preview(showBackground = true)
@Composable
fun PreviewModernAnimatedLoginForm() {
    com.avinashpatil.app.dishacomputer.ui.theme.DISHAComputerTheme {
        ModernAnimatedLoginForm(
            navController = rememberNavController()
        )
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewModernAnimatedLoginFormDark() {
    com.avinashpatil.app.dishacomputer.ui.theme.DISHAComputerTheme(darkTheme = true) {
        ModernAnimatedLoginForm(
            navController = rememberNavController()
        )
    }
}
