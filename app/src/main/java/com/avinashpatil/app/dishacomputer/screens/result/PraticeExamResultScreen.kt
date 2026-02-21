package com.avinashpatil.app.dishacomputer.screens.result

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.avinashpatil.app.dishacomputer.navigation.Screen
import com.avinashpatil.app.dishacomputer.ui.theme.*

// Define Colors from HTML


@Composable
fun PracticeExamResultScreen(
    navController: NavController,
    score: Int,
    totalScore: Int,
    correctAnswers: Int,
    wrongAnswers: Int,
    skippedAnswers: Int,
    topicId: String,
    source: String = "TOPIC"
) {
    PracticeExamResultScreenContent(
        navController = navController,
        score = score,
        totalScore = totalScore,
        correctAnswers = correctAnswers,
        wrongAnswers = wrongAnswers,
        skippedAnswers = skippedAnswers,
        topicId = topicId,
        source = source
    )
}

@Composable
fun PracticeExamResultScreenContent(
    navController: NavController,
    score: Int,
    totalScore: Int,
    correctAnswers: Int,
    wrongAnswers: Int,
    skippedAnswers: Int,
    topicId: String,
    source: String = "TOPIC"
) {
    // Calculate Accuracy
    val accuracyValue = if (totalScore > 0) (correctAnswers * 100) / totalScore else 0
    
    // Star Logic
    val stars = when {
        accuracyValue <= 20 -> 1
        accuracyValue <= 40 -> 2
        accuracyValue <= 60 -> 3
        accuracyValue <= 80 -> 4
        else -> 5
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Mesh Gradient Background
        MeshGradientBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 0.dp, start = 24.dp, end = 24.dp, bottom = 12.dp)
                ) {
                    Header(
                        onClose = {
                            if (source == "EXAM") {
                                navController.navigate(Screen.Exams.route) {
                                    popUpTo(Screen.Exams.route) { inclusive = true }
                                }
                            } else {
                                // Close -> TopicsScreen
                                navController.navigate(Screen.Topics.route) {
                                    popUpTo(Screen.Topics.route) { inclusive = true }
                                }
                            }
                        }
                    )
                }
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(top = 12.dp, start = 24.dp, end = 24.dp, bottom = 0.dp)
                )
            }
        ) { innerPadding ->
            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Score Card
                ScoreCard(score, totalScore, stars)
                
                Spacer(modifier = Modifier.height(24.dp))

                // Stats Row: Correct, Wrong, Skipped
                StatsRow(correctAnswers, wrongAnswers, skippedAnswers)

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun MeshGradientBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Top Left - Pinkish
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MeshPink.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(0f, 0f),
                radius = width * 0.8f
            )
        )
        
        // Top Right - Purpleish
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MeshPurple.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(width, 0f),
                radius = width * 0.8f
            )
        )

        // Bottom Right - Red/Pink
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MeshRed.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(width, height),
                radius = width * 0.8f
            )
        )

        // Bottom Left - Blue/Purple
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MeshDeepPurple.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(0f, height),
                radius = width * 0.8f
            )
        )
    }
}

@Composable
private fun Header(onClose: () -> Unit) {
    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = MaterialTheme.colorScheme.outlineVariant
    val contentColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Close Button (Glass Card)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(cardBg)
                .border(1.dp, cardBorder, CircleShape)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        }

        // Title
        Text(
            text = "Exam Complete!",
            color = contentColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )

        // Spacer to balance the layout (empty 40dp box)
        Box(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun ScoreCard(score: Int, totalScore: Int, stars: Int) {
    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = MaterialTheme.colorScheme.outlineVariant
    val contentColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circular Progress
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(176.dp) 
            ) {
                val shadowColor = MaterialTheme.colorScheme.tertiary
                // Shadow effect
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = shadowColor.copy(alpha = 0.05f),
                        radius = size.minDimension / 2 + 20.dp.toPx()
                    )
                }

                CircularProgress(
                    progress = if (totalScore > 0) score / totalScore.toFloat() else 0f,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Score Text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(color = contentColor, fontSize = 52.sp, fontWeight = FontWeight.Black)) {
                                append(score.toString())
                            }
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.tertiary, fontSize = 24.sp, fontWeight = FontWeight.Bold)) {
                                append("/$totalScore")
                            }
                        },
                        lineHeight = 52.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Congratulatory Text
            Text(
                text = "अभिनंदन!",
                color = contentColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "तुम्ही ही परीक्षा यशस्वीरीत्या पूर्ण केल्याबद्दल तुमचे अभिनंदन आणि तुम्हाला पुढील परीक्षांसाठी खूप खूप शुभेच्छा.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            // Divider
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stars
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(5) { index ->
                    val isFilled = index < stars
                    Icon(
                        imageVector = if (isFilled) Icons.Default.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (isFilled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                        modifier = Modifier.size(28.dp).padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CircularProgress(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val progressColor = MaterialTheme.colorScheme.tertiary
    
    Canvas(modifier = modifier) {
        val strokeWidth = 4.dp.toPx()
        val diameter = size.minDimension
        val radius = diameter / 2 - strokeWidth / 2
        
        // Background Circle (Track)
        drawCircle(
            color = progressColor.copy(alpha = 0.2f),
            radius = radius,
            style = Stroke(width = strokeWidth)
        )

        // Progress Arc
        drawArc(
            color = progressColor,
            startAngle = -90f,
            sweepAngle = 360 * progress,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(diameter - strokeWidth, diameter - strokeWidth)
        )
    }
}

@Composable
private fun StatsRow(correct: Int, wrong: Int, skipped: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatItem(
            icon = Icons.Default.CheckCircle,
            iconColor = QuizCorrect,
            label = "Correct",
            value = correct.toString(),
            modifier = Modifier.weight(1f)
        )
        StatItem(
            icon = Icons.Default.Cancel,
            iconColor = QuizWrong,
            label = "Wrong",
            value = wrong.toString(),
            modifier = Modifier.weight(1f)
        )
        StatItem(
            icon = Icons.Default.SkipNext,
            iconColor = MaterialTheme.colorScheme.tertiary,
            label = "Skipped",
            value = skipped.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val pillBg = MaterialTheme.colorScheme.surface
    val pillBorder = MaterialTheme.colorScheme.outlineVariant
    val contentColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp)) // glass-pill rounded-lg
            .background(pillBg)
            .border(1.dp, pillBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp).padding(bottom = 6.dp)
        )
        Text(
            text = label.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = value,
            color = contentColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PracticeExamResultScreenPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    DISHAComputerTheme {
        PracticeExamResultScreenContent(
            navController = navController,
            score = 85,
            totalScore = 100,
            correctAnswers = 85,
            wrongAnswers = 10,
            skippedAnswers = 5,
            topicId = "1"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PracticeExamResultScreenDarkPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    DISHAComputerTheme(darkTheme = true) {
        PracticeExamResultScreenContent(
            navController = navController,
            score = 85,
            totalScore = 100,
            correctAnswers = 85,
            wrongAnswers = 10,
            skippedAnswers = 5,
            topicId = "1"
        )
    }
}
/*
@Composable
private fun RetryButton(onRetry: () -> Unit) {
    Button(
        onClick = onRetry,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryPink
        ),
        shape = CircleShape,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Retry",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}


 */