package com.avinashpatil.app.dishacomputer.screens.result

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.avinashpatil.app.dishacomputer.components.CenteredErrorDisplay
import com.avinashpatil.app.dishacomputer.navigation.Screen
import com.avinashpatil.app.dishacomputer.viewmodel.CertificationExamResultViewModel
import com.avinashpatil.app.dishacomputer.viewmodel.CertificationResultUiState
import com.avinashpatil.app.dishacomputer.ui.theme.*

// Define Colors from HTML


// Data Models for Preview
data class ReviewQuestion(
    val id: Int,
    val questionMarathi: String,
    val questionEnglish: String,
    val options: List<List_Option>,
    val correctOptionId: String,
    val selectedOptionId: String?, // Null if skipped
    val marks: Int
)

data class List_Option(
    val id: String,
    val textMarathi: String,
    val textEnglish: String
)

@Composable
fun CertificationExamResultScreen(
    navController: NavController,
    quizId: String,
    examId: String,
    viewModel: CertificationExamResultViewModel = hiltViewModel()
) {
    LaunchedEffect(quizId, examId) {
        viewModel.loadData(quizId, examId)
    }

    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is CertificationResultUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                MeshGradientBackground()
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is CertificationResultUiState.Error -> {
            CenteredErrorDisplay(
                message = state.message,
                onRetry = { viewModel.loadData(quizId, examId) }
            )
        }
        is CertificationResultUiState.Success -> {
            CertificationExamResultContent(
                score = state.score,
                totalScore = state.totalScore,
                correctAnswers = state.correctAnswers,
                wrongAnswers = state.wrongAnswers,
                accuracy = state.accuracy,
                examTitleMarathi = state.examTitleMarathi,
                examTitleEnglish = state.examTitleEnglish,
                duration = state.duration,
                date = state.date,
                time = state.time,
                questions = state.questions,
                onClose = { navController.navigate(Screen.Exams.route) },
                skippedQuestions = state.skippedQuestions,
                accuracyVal = state.accuracyVal
            )
        }
    }
}

@Composable
fun CertificationExamResultContent(
    score: Int = 0,
    totalScore: Int = 0,
    correctAnswers: Int = 0,
    wrongAnswers: Int = 0,
    skippedQuestions: Int = 0,
    accuracy: String = "0%",
    accuracyVal: Int = 0,
    examTitleMarathi: String = "",
    examTitleEnglish: String = "",
    duration: String = "",
    date: String = "",
    time: String = "",
    questions: List<ReviewQuestion> = emptyList(),
    onClose: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                    Header(onClose = onClose)
                }
            },
            /*
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(top = 12.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
                ) {
                    FinishButton(onClick = onClose)
                }
            }
             */
        ) { innerPadding ->
            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    ScoreSection(score, totalScore, correctAnswers, wrongAnswers, skippedQuestions, accuracyVal)
                }

                item {
                    ExamDetailSection(examTitleMarathi, examTitleEnglish, duration, date, time)
                }

                item {
                    Text(
                        text = "REVIEW QUESTIONS",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }

                items(questions) { question ->
                    QuestionItemCard(question)
                }
            }
        }
    }
}
/*
@Composable
private fun FinishButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
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
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Finish Review",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}


 */
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
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Close Button (Glass Card)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }

        // Title
        Text(
            text = "Exam Complete!",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )

        // Spacer to balance the layout
        Box(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun ScoreSection(
    score: Int,
    totalScore: Int,
    correctAnswers: Int,
    wrongAnswers: Int,
    skippedQuestions: Int,
    accuracyVal: Int
) {
    val stars = when {
        accuracyVal <= 20 -> 1
        accuracyVal <= 40 -> 2
        accuracyVal <= 60 -> 3
        accuracyVal <= 80 -> 4
        else -> 5
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)) // rounded-xl
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular Progress
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(128.dp) // w-32 h-32
            ) {
                val shadowColor = MaterialTheme.colorScheme.tertiary
                // Shadow
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = shadowColor.copy(alpha = 0.05f),
                        radius = size.minDimension / 2 + 15.dp.toPx()
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
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 32.sp, fontWeight = FontWeight.Black)) {
                                append(score.toString())
                            }
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.tertiary, fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                                append("/$totalScore")
                            }
                        },
                        lineHeight = 32.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Star Rating
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                repeat(5) { index ->
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (index < stars) StarGold else MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Text
            Text(
                text = "अभिनंदन!",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "तुम्ही ही परीक्षा यशस्वीरीत्या पूर्ण केली आहे.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill(Icons.Default.CheckCircle, QuizCorrect, "Correct", correctAnswers.toString(), Modifier.weight(1f))
                StatPill(Icons.Default.Cancel, QuizWrong, "Wrong", wrongAnswers.toString(), Modifier.weight(1f))
                StatPill(Icons.Default.Help, MaterialTheme.colorScheme.tertiary, "Skipped", skippedQuestions.toString(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatPill(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp)) // glass-pill rounded-lg
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp).padding(bottom = 4.dp)
        )
        Text(
            text = label.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ExamDetailSection(
    titleMarathi: String,
    titleEnglish: String,
    duration: String,
    date: String,
    time: String
) {
    val title = if (titleMarathi.isNotBlank() && titleEnglish.isNotBlank() && titleMarathi != titleEnglish) {
        "$titleMarathi / $titleEnglish"
    } else {
        titleMarathi.ifBlank { titleEnglish }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EXAM TITLE",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            // Grid for Duration and Schedule
            Row(modifier = Modifier.fillMaxWidth()) {
                // Duration
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DURATION",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = duration,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Schedule
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SCHEDULE",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = BrandPink,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = date,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = time,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(start = 24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionItemCard(question: ReviewQuestion) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Q #${question.id}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = if (question.marks > 0) "+${question.marks} Marks" else "${question.marks} Marks",
                    color = if (question.marks > 0) CorrectGreen else WrongOrange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            // Content
            Column(modifier = Modifier.padding(16.dp)) {
                // Question Text
                Text(
                    text = question.questionMarathi,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = question.questionEnglish,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Options
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    question.options.forEach { option ->
                        val isCorrect = option.id == question.correctOptionId
                        val isSelected = option.id == question.selectedOptionId
                        val isWrongSelection = isSelected && !isCorrect
                        
                        // Status Colors
                        val borderColor = when {
                            isCorrect -> QuizCorrect.copy(alpha = 0.4f)
                            isWrongSelection -> QuizWrong.copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }
                        
                        val backgroundColor = when {
                            isCorrect -> QuizCorrect.copy(alpha = 0.1f)
                            isWrongSelection -> QuizWrong.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(backgroundColor)
                                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option.id,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.textMarathi,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = option.textEnglish,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }

                            if (isCorrect) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Correct",
                                    tint = QuizCorrect,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else if (isWrongSelection) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Wrong",
                                    tint = QuizWrong,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
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
        val strokeWidth = 3.dp.toPx() // Thinner stroke for smaller circle
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

// Sample Data
private val sampleQuestions = listOf(
    ReviewQuestion(
        id = 1,
        questionMarathi = "पाण्याचा उत्कलन बिंदू किती आहे?",
        questionEnglish = "What is the boiling point of water?",
        options = listOf(
            List_Option("A", "१००° सेल्सिअस", "100° Celsius"),
            List_Option("B", "५०° सेल्सिअस", "50° Celsius"),
            List_Option("C", "०° सेल्सिअस", "0° Celsius"),
            List_Option("D", "२००° सेल्सिअस", "200° Celsius")
        ),
        correctOptionId = "A",
        selectedOptionId = "A",
        marks = 2
    ),
    ReviewQuestion(
        id = 2,
        questionMarathi = "मानवी शरीरातील सर्वात मोठे हाड कोणते?",
        questionEnglish = "Which is the largest bone in the human body?",
        options = listOf(
            List_Option("A", "स्टेपीज", "Stapes"),
            List_Option("B", "फिमर", "Femur"),
            List_Option("C", "ह्युमेरस", "Humerus"),
            List_Option("D", "तिबिया", "Tibia")
        ),
        correctOptionId = "B",
        selectedOptionId = "A",
        marks = 0
    )
)

@Preview(showBackground = true)
@Composable
fun PreviewCertificationExamResultScreen() {
    DISHAComputerTheme {
        CertificationExamResultContent(
            score = 80,
            totalScore = 100,
            correctAnswers = 40,
            wrongAnswers = 10,
            skippedQuestions = 0,
            accuracy = "80%",
            accuracyVal = 80,
            examTitleMarathi = "संगणक परिचय",
            examTitleEnglish = "Introduction to Computers",
            duration = "30 Min",
            date = "28 Jan 2024",
            time = "10:30 AM",
            questions = sampleQuestions
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewCertificationExamResultScreenDark() {
    DISHAComputerTheme {
        CertificationExamResultContent(
            score = 80,
            totalScore = 100,
            correctAnswers = 40,
            wrongAnswers = 10,
            skippedQuestions = 0,
            accuracy = "80%",
            accuracyVal = 80,
            examTitleMarathi = "संगणक परिचय",
            examTitleEnglish = "Introduction to Computers",
            duration = "30 Min",
            date = "28 Jan 2024",
            time = "10:30 AM",
            questions = sampleQuestions
        )
    }
}
