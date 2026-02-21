package com.avinashpatil.app.dishacomputer.screens.dishaAI

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.avinashpatil.app.dishacomputer.R
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.IconButton
import coil.compose.AsyncImage
import com.avinashpatil.app.dishacomputer.ui.theme.*

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import com.avinashpatil.app.dishacomputer.viewmodel.ModernDishaAIViewModel
import com.avinashpatil.app.dishacomputer.data.model.ChatMessage

@Composable
fun ModernDishaAIScreen(
    viewModel: ModernDishaAIViewModel = viewModel()
) {
    // State
    var chatInput by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    val messages by viewModel.messages.collectAsState()
    
    // Internalized Theme Detection
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    ModernDishaAIScreenContent(
        messages = messages,
        chatInput = chatInput,
        isTyping = isTyping,
        showClearDialog = showClearDialog,
        onShowClearDialogChange = { showClearDialog = it },
        onChatInputChange = { chatInput = it },
        onSend = {
            if (chatInput.isNotBlank()) {
                viewModel.sendMessage(chatInput) { typing ->
                    isTyping = typing
                }
                chatInput = ""
            }
        },
        onClearChat = {
            viewModel.clearChat()
            showClearDialog = false
        }
    )
}

@Composable
fun ModernDishaAIScreenContent(
    messages: List<ChatMessage>,
    chatInput: String,
    isTyping: Boolean,
    showClearDialog: Boolean,
    onShowClearDialogChange: (Boolean) -> Unit,
    onChatInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onClearChat: () -> Unit
) {
    // Internalized Theme Detection
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Colors
    val primaryColor = if (isDarkTheme) GradientIndigoEnd else PrimaryBlue
    val accentBlue = if (isDarkTheme) PrimaryBlue else PrimaryBlueDark
    val textMain = if (isDarkTheme) Slate50 else Slate800
    val textSecondary = if (isDarkTheme) Slate400 else Slate500
    
    val bgGradientColors = if (isDarkTheme) {
        listOf(
            Slate900,
            Slate900,
            Slate900
        )
    } else {
        listOf(
            Indigo100,
            Emerald50,
            Sky100
        )
    }

    // Gradients
    val bgGradient = Brush.linearGradient(
        colors = bgGradientColors,
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    // Mesh Gradient Layers
    val meshAlpha = if (isDarkTheme) 0.1f else 0.2f
    val meshLayer1 = Brush.radialGradient(
        colors = listOf(AccentPurple.copy(alpha = meshAlpha), Color.Transparent),
        center = Offset(0f, 0f),
        radius = 1000f
    )
    val meshLayer2 = Brush.radialGradient(
        colors = listOf(Emerald400.copy(alpha = meshAlpha), Color.Transparent),
        center = Offset(Float.POSITIVE_INFINITY, 0f),
        radius = 1000f
    )
    val meshLayer3 = Brush.radialGradient(
        colors = listOf(Sky500.copy(alpha = meshAlpha), Color.Transparent),
        center = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        radius = 1000f
    )
    val meshLayer4 = Brush.radialGradient(
        colors = listOf(PrimaryIndigo.copy(alpha = meshAlpha * 0.5f), Color.Transparent),
        center = Offset(0f, Float.POSITIVE_INFINITY),
        radius = 1000f
    )

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { onShowClearDialogChange(false) },
            title = { Text("Clear chat?") },
            text = { Text("This will remove all DISHA AI conversations.") },
            confirmButton = {
                TextButton(onClick = onClearChat) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { onShowClearDialogChange(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            ChatFooter(
                primaryColor = primaryColor,
                chatInput = chatInput,
                onChatInputChange = onChatInputChange,
                onSend = onSend
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .background(meshLayer1)
                .background(meshLayer2)
                .background(meshLayer3)
                .background(meshLayer4)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header
                ChatHeader(
                    primaryColor, 
                    textMain, 
                    textSecondary, 
                    onNewChat = { onShowClearDialogChange(true) }
                )

                // Chat Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "TODAY",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500,
                                    letterSpacing = 1.sp
                                ),
                                modifier = Modifier
                                    .shadow(1.dp, RoundedCornerShape(50))
                                    .background(
                                        if (isDarkTheme) Black.copy(alpha = 0.4f) else White.copy(alpha = 0.4f), 
                                        RoundedCornerShape(50)
                                    )
                                    .border(
                                        1.dp,
                                        if (isDarkTheme) White.copy(alpha = 0.2f) else White.copy(alpha = 0.7f),
                                        RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Initial Welcome Message
                    if (messages.isEmpty()) {
                        item {
                            ReceiverMessage(
                                message = "Hello! I'm DISHA AI. How can I assist your Computer journey today?",
                                avatarUrl = "",
                                name = "DISHA AI",
                                textSecondary = textSecondary,
                                textMain = textMain,
                                useLocalAvatar = true
                            )
                        }
                    }

                    // Dynamic Messages
                    items(messages.size) { index ->
                        val message = messages[index]
                        if (message.isUser) {
                            SenderMessage(
                                message = message.text,
                                avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuC7aj0gC3ch10lNyY2nt17hkZ7SZ3x1hNalyMq4lwhOgzjbm7qFLpF5bUJ2iWw0hiq8zLduM3LxbMkMaz0fqxYt9n5fKKHFgLcCUD7gOoDgAT_2WrTh-wT_tZVZaXmA1h_ghJ48H_tV_sfxSiFargJRK3XHWXEoRZyr2mCpL6wWCuYzfCWjAv-F0SSrYJX75nC24U8YX0XI-L8tHn8_ZwfyZidUZ_zm-bmzBH11oOroBCtcWXSQs7aERexBSAqFygHGWbxk0RHNMAqU",
                                name = "You",
                                textSecondary = textSecondary
                            )
                        } else {
                            ReceiverMessage(
                                message = message.text,
                                avatarUrl = "",
                                name = "DISHA AI",
                                textSecondary = textSecondary,
                                textMain = textMain,
                                useLocalAvatar = true
                            )
                        }
                    }

                    if (isTyping) {
                        item {
                            TypingIndicator(
                                avatarUrl = "",
                                primaryColor = primaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatHeader(
    primaryColor: Color,
    textMain: Color,
    textSecondary: Color,
    onNewChat: () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDarkTheme) Black.copy(alpha = 0.4f) else White.copy(alpha = 0.4f))
            .border(
                width = 1.dp,
                color = if (isDarkTheme) White.copy(alpha = 0.1f) else White.copy(alpha = 0.4f),
                shape = RoundedCornerShape(0.dp)
            )
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Avatar with border
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isDarkTheme) Slate800 else White)
                    .border(2.dp, primaryColor.copy(alpha = 0.2f), CircleShape)
                    .padding(2.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Disha AI Avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Column {
                Text(
                    text = "DISHA AI",
                    style = TextStyle(
                        color = textMain,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PulsingDot()
                    Text(
                        text = "ACTIVE NOW",
                        style = TextStyle(
                            color = textSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }
        
        // New Chat Icon
        IconButton(onClick = onNewChat) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Chat",
                tint = textMain
            )
        }
    }
}

@Composable
fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(contentAlignment = Alignment.Center) {
        // Ripple effect
        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(scale)
                .background(SuccessGreen.copy(alpha = alpha * 0.4f), CircleShape)
        )
        // Solid dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(SuccessGreen, CircleShape)
        )
    }
}

@Composable
fun ReceiverMessage(
    message: String,
    avatarUrl: String,
    name: String,
    textSecondary: Color,
    textMain: Color,
    useLocalAvatar: Boolean = false
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 48.dp), // Max width approx 85%
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isDarkTheme) White.copy(alpha = 0.1f) else White.copy(alpha = 0.4f))
                .border(1.dp, if (isDarkTheme) White.copy(alpha = 0.2f) else White, CircleShape)
        ) {
            if (useLocalAvatar) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = name.uppercase(),
                style = TextStyle(
                    color = textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            Box(
                modifier = Modifier
                    .shadow(1.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                    .background(
                        if (isDarkTheme) Black.copy(alpha = 0.4f) else White.copy(alpha = 0.4f),
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp)
                    )
                    .border(
                        1.dp,
                        if (isDarkTheme) White.copy(alpha = 0.2f) else White.copy(alpha = 0.7f),
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message,
                    style = TextStyle(
                        color = textMain,
                        fontSize = 15.sp, // md:text-base
                        fontWeight = FontWeight.Normal,
                        lineHeight = 24.sp
                    )
                )
            }
        }
    }
}

@Composable
fun SenderMessage(
    message: String,
    avatarUrl: String,
    name: String,
    textSecondary: Color
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp), // Max width constraint
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = name.uppercase(),
                style = TextStyle(
                    color = textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
            )

            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 15.dp,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 4.dp, bottomStart = 16.dp)
                    )
                    .background(
                        brush = Brush.linearGradient(
                            colors = if (isDarkTheme) {
                                listOf(GradientIndigoEnd, PrimaryBlue)
                            } else {
                                listOf(PrimaryBlue, PrimaryBlueDark)
                            }
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 4.dp, bottomStart = 16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message,
                    style = TextStyle(
                        color = White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 24.sp
                    )
                )
            }
        }

        // Avatar
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .border(2.dp, if (isDarkTheme) White.copy(alpha = 0.2f) else White, CircleShape)
                .shadow(1.dp, CircleShape)
        ) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun TypingIndicator(
    avatarUrl: String,
    primaryColor: Color
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isDarkTheme) White.copy(alpha = 0.1f) else White.copy(alpha = 0.4f))
                .border(1.dp, if (isDarkTheme) White.copy(alpha = 0.2f) else White, CircleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.5f), // Opacity 50%
                contentScale = ContentScale.Crop
            )
        }

        // Dots Container
        Row(
            modifier = Modifier
                .shadow(1.dp, RoundedCornerShape(16.dp))
                .background(
                    if (isDarkTheme) Black.copy(alpha = 0.4f) else White.copy(alpha = 0.4f),
                    RoundedCornerShape(16.dp)
                )
                .border(
                    1.dp,
                    if (isDarkTheme) White.copy(alpha = 0.2f) else White.copy(alpha = 0.7f),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BouncingDot(delayMillis = 0, color = primaryColor)
            BouncingDot(delayMillis = 200, color = primaryColor)
            BouncingDot(delayMillis = 400, color = primaryColor)
        }
    }
}

@Composable
fun BouncingDot(delayMillis: Int, color: Color) {
    val animatable = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        // Initial delay
        kotlinx.coroutines.delay(delayMillis.toLong())
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    // Use translateY for bounce effect
    Box(
        modifier = Modifier
            .size(6.dp)
            .background(color.copy(alpha = 0.4f), CircleShape)
            // Simple visual bounce using offset is easier than physics
            .padding(bottom = (animatable.value * 4).dp) 
    )
}

// Better Bouncing Dot implementation using graphicsLayer or offset
@Composable
fun BouncingDot2(delayMillis: Int, color: Color) {
   // ... implementation using infiniteTransition
}

// Actually Modifier.alpha exists in compose ui draw

@Composable
fun ChatFooter(
    primaryColor: Color,
    chatInput: String,
    onChatInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDarkTheme) {
                        listOf(
                            Black.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    } else {
                        listOf(
                            White.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    },
                    startY = Float.POSITIVE_INFINITY,
                    endY = 0f
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(50)
                )
                .background(if (isDarkTheme) Slate800 else White, RoundedCornerShape(50))
                .border(1.dp, if (isDarkTheme) White.copy(alpha = 0.1f) else White.copy(alpha = 0.5f), RoundedCornerShape(50))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Input
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                BasicTextField(
                    value = chatInput,
                    onValueChange = onChatInputChange,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = if (isDarkTheme) Slate50 else Slate800
                    ),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (chatInput.isEmpty()) { // Show placeholder logic
                                Text(
                                    text = "Message DISHA...",
                                    style = TextStyle(
                                        color = if (isDarkTheme) Slate400 else Slate400,
                                        fontSize = 16.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    cursorBrush = SolidColor(if (isDarkTheme) Slate50 else Slate800)
                )
            }

            // Send Button
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .size(40.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = CircleShape
                    )
                    .background(primaryColor, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowUpward,
                    contentDescription = "Send",
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun PreviewModernDishaAIScreenLight() {
    val dummyMessages = listOf(
        ChatMessage(text = "Hello! I'm DISHA AI. How can I assist your Computer journey today?", isUser = false),
        ChatMessage(text = "I need help with Python.", isUser = true),
        ChatMessage(text = "Sure! I can help you with Python. What specific topic are you interested in?", isUser = false)
    )
    ModernDishaAIScreenContent(
        messages = dummyMessages,
        chatInput = "Type a message...",
        isTyping = false,
        showClearDialog = false,
        onShowClearDialogChange = {},
        onChatInputChange = {},
        onSend = {},
        onClearChat = {}
    )
}

@Preview(name = "Dark Mode", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewModernDishaAIScreenDark() {
    val dummyMessages = listOf(
        ChatMessage(text = "Hello! I'm DISHA AI. How can I assist your Computer journey today?", isUser = false),
        ChatMessage(text = "I need help with Python.", isUser = true),
        ChatMessage(text = "Sure! I can help you with Python. What specific topic are you interested in?", isUser = false)
    )
    ModernDishaAIScreenContent(
        messages = dummyMessages,
        chatInput = "Type a message...",
        isTyping = true,
        showClearDialog = false,
        onShowClearDialogChange = {},
        onChatInputChange = {},
        onSend = {},
        onClearChat = {}
    )
}
