package com.avinashpatil.app.dishacomputer.screens.instructions

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

import com.avinashpatil.app.dishacomputer.ui.theme.DISHAComputerTheme
import com.avinashpatil.app.dishacomputer.ui.theme.PrimaryIndigo
import com.avinashpatil.app.dishacomputer.ui.theme.AccentPurple
import com.avinashpatil.app.dishacomputer.ui.theme.AccentPink
import com.avinashpatil.app.dishacomputer.ui.theme.AccentOrange
import com.avinashpatil.app.dishacomputer.ui.theme.AccentCyan
import com.avinashpatil.app.dishacomputer.ui.theme.AccentEmerald
import com.avinashpatil.app.dishacomputer.ui.theme.Indigo600
import com.avinashpatil.app.dishacomputer.ui.theme.Purple600
import com.avinashpatil.app.dishacomputer.ui.theme.Pink600

@Composable
fun ModernInstructionsScreen(
    onStartExamClick: () -> Unit = {},
    attempted: Boolean = false
) {
    // Colors
    val context = LocalContext.current
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    
    // Mesh Gradient Background
    val meshGradientBrush = Brush.radialGradient(
        colors = listOf(
            PrimaryIndigo.copy(alpha = if (isDarkTheme) 0.15f else 0.05f),
            Color.Transparent
        ),
        center = Offset(0f, 0f),
        radius = 1000f
    )
    
    val meshGradientBrush2 = Brush.radialGradient(
        colors = listOf(
            AccentPurple.copy(alpha = if (isDarkTheme) 0.15f else 0.05f),
            Color.Transparent
        ),
        center = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY), // Bottom right approx
        radius = 1000f
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            InstructionsFooter(
                onStartClick = {
                    if (attempted) {
                        Toast.makeText(
                            context,
                            "You have already submitted this certification exam.\nRe-attempt is not allowed.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        onStartExamClick()
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .background(meshGradientBrush)
        ) {
            // Second gradient layer for bottom-right
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AccentPurple.copy(alpha = if (isDarkTheme) 0.15f else 0.05f),
                                Color.Transparent
                            ),
                            center = Offset(1000f, 2000f), // Approximate bottom right
                            radius = 1500f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header
                InstructionsHeader()

                // Main Content
                InstructionsList(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun InstructionsHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, bottom = 16.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gradient Text
        val gradientColors = listOf(
            PrimaryIndigo,
            AccentPurple,
            AccentPink
        )
        
        Text(
            text = "Exam Instructions",
            style = TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                brush = Brush.linearGradient(colors = gradientColors)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Please review the rules before starting your session.",
            style = TextStyle(
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal
            ),
            textAlign = TextAlign.Center
        )
    }
}

data class InstructionItemData(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val color: Color
)

@Composable
fun InstructionsList(modifier: Modifier = Modifier) {
    val instructions = listOf(
        InstructionItemData(
            icon = Icons.Rounded.Wifi,
            title = "Stable Connection",
            description = "Ensure you have a stable internet connection throughout the exam.",
            color = PrimaryIndigo
        ),
        InstructionItemData(
            icon = Icons.Rounded.Badge,
            title = "ID Verification",
            description = "Keep your ID proof ready for verification before the exam starts.",
            color = AccentPurple
        ),
        InstructionItemData(
            icon = Icons.Rounded.Videocam,
            title = "Hardware Check",
            description = "Make sure your webcam and microphone are working properly.",
            color = AccentPink
        ),
        InstructionItemData(
            icon = Icons.Rounded.Devices,
            title = "Device Restriction",
            description = "No other electronic devices allowed except the exam computer.",
            color = AccentOrange
        ),
        InstructionItemData(
            icon = Icons.Rounded.VolumeOff,
            title = "Quiet Environment",
            description = "Stay in a quiet room to avoid any disturbances during the test.",
            color = AccentCyan
        ),
        InstructionItemData(
            icon = Icons.Rounded.Lock,
            title = "No Screen Exit",
            description = "You are not allowed to leave the exam screen during the test duration.",
            color = AccentEmerald
        )
    )

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(instructions) { item ->
            InstructionCard(item)
        }
    }
}

@Composable
fun InstructionCard(item: InstructionItemData) {
    // Glass card style
    val cardBackground = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    val cardBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val titleColor = MaterialTheme.colorScheme.onSurface
    val descColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            // Left border simulation
            .drawBehind {
                drawRect(
                    color = item.color,
                    topLeft = Offset.Zero,
                    size = size.copy(width = 4.dp.toPx())
                )
            }
            .clickable { }
            .padding(16.dp) // Inner padding
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Text Content
            Column {
                Text(
                    text = item.title,
                    style = TextStyle(
                        color = titleColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif // Or specific font if available
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = TextStyle(
                        color = descColor, // Slate 400
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }
    }
}

@Composable
fun InstructionsFooter(onStartClick: () -> Unit) {
    val footerGradientColors = listOf(
        MaterialTheme.colorScheme.background.copy(alpha = 0.0f), // Transparent
        MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
        MaterialTheme.colorScheme.background
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = footerGradientColors
                )
            )
            .padding(24.dp)
            .padding(bottom = 16.dp) // Extra padding for bottom indicator space
    ) {
        // Start Exam Button
        val buttonGradient = Brush.horizontalGradient(
            colors = listOf(
                Indigo600,
                Purple600,
                Pink600
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(buttonGradient)
                .clickable(onClick = onStartClick),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "START EXAM",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                )
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Preview(name = "Light Mode")
@Composable
fun ModernInstructionsScreenPreviewLight() {
    DISHAComputerTheme(darkTheme = false) {
        ModernInstructionsScreen()
    }
}

@Preview(name = "Dark Mode")
@Composable
fun ModernInstructionsScreenPreviewDark() {
    DISHAComputerTheme(darkTheme = true) {
        ModernInstructionsScreen()
    }
}
