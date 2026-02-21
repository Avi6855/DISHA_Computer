package com.avinashpatil.app.dishacomputer.screens.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.avinashpatil.app.dishacomputer.components.CenteredErrorDisplay
import com.avinashpatil.app.dishacomputer.navigation.Screen
import com.avinashpatil.app.dishacomputer.viewmodel.QuizViewModel
import com.avinashpatil.app.dishacomputer.viewmodel.QuizUiState
import android.media.AudioManager
import android.media.ToneGenerator
import com.avinashpatil.app.dishacomputer.data.model.Question
import com.avinashpatil.app.dishacomputer.data.model.QuestionType
import com.avinashpatil.app.dishacomputer.components.CenteredErrorDisplay

import com.avinashpatil.app.dishacomputer.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeQuizScreen(
    navController: NavController,
    viewModel: QuizViewModel = hiltViewModel(),
    courseId: String? = null,
    topicId: String? = null,
    limit: Int = 25,
    source: String = "TOPIC",
    modifier: Modifier = Modifier
) {
    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsState(initial = QuizUiState.Loading)
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState(initial = 0)
    val selectedAnswer by viewModel.selectedAnswer.collectAsState(initial = -1)
    val questionTimeLeft by viewModel.questionTimeLeft.collectAsState(initial = 30)
    val totalTimeLeft by viewModel.totalTimeLeft.collectAsState(initial = 1800)
    val isPaused by viewModel.isPaused.collectAsState(initial = false)
    
    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }

    // Load quiz when screen is first displayed
    LaunchedEffect(courseId, topicId) {
        if (topicId != null) {
            viewModel.loadQuizByTopic(topicId, limit)
        } else {
            viewModel.loadQuiz(courseId, limit)
        }
    }

    // Log the current UI state for debugging
    LaunchedEffect(uiState) {
        when (uiState) {
            is QuizUiState.Loading -> println("QuizUiState: Loading")
            is QuizUiState.Error -> println("QuizUiState: Error - ${(uiState as QuizUiState.Error).message}")
            is QuizUiState.Success -> println("QuizUiState: Success - ${(uiState as QuizUiState.Success).questions.size} questions loaded")
        }
    }

    PracticeQuizContent(
        uiState = uiState,
        currentQuestionIndex = currentQuestionIndex,
        selectedAnswer = selectedAnswer,
        questionTimeLeft = questionTimeLeft,
        totalTimeLeft = totalTimeLeft.toLong(),
        isPaused = isPaused,
        isSubmitting = isSubmitting,
        submitError = submitError,
        showExitDialog = showExitDialog,
        onOptionSelected = { viewModel.selectAnswer(it) },
        onPreviousQuestion = { viewModel.goToPreviousQuestion() },
        onNextQuestion = { viewModel.goToNextQuestion() },
        onTogglePause = { viewModel.togglePause() },
        onFinishQuiz = {
            if (uiState is QuizUiState.Success) {
                val questions = (uiState as QuizUiState.Success).questions
                // Calculate results locally
                val answers = viewModel.answersByQuestionIndex.value
                var correct = 0
                var wrong = 0
                var skipped = 0
                
                questions.forEachIndexed { index, question ->
                    val answerIdx = answers[index]
                    if (answerIdx == null || answerIdx !in 0..3) {
                        skipped++
                    } else if (answerIdx == question.correctOptionIndex) {
                        correct++
                    } else {
                        wrong++
                    }
                }
                
                val score = correct
                val total = questions.size
                val validTopicId = topicId ?: "unknown"

                navController.navigate(
                    "${Screen.PracticeExamResult.route}/$score/$total/$correct/$wrong/$skipped/$validTopicId?source=$source"
                ) {
                    popUpTo(Screen.PracticeQuiz.route) { inclusive = true }
                }
            }
        },
        onDismissSubmitError = { submitError = null },
        onDismissExitDialog = { showExitDialog = false },
        onConfirmExit = {
            showExitDialog = false
            navController.popBackStack()
        },
        onRetry = {
            if (topicId != null) {
                viewModel.loadQuizByTopic(topicId, limit)
            } else {
                viewModel.loadQuiz(courseId, limit)
            }
        },
        modifier = modifier
    )
}

@Composable
fun PracticeQuizContent(
    uiState: QuizUiState,
    currentQuestionIndex: Int,
    selectedAnswer: Int,
    questionTimeLeft: Int,
    totalTimeLeft: Long,
    isPaused: Boolean,
    isSubmitting: Boolean,
    submitError: String?,
    showExitDialog: Boolean,
    onOptionSelected: (Int) -> Unit,
    onPreviousQuestion: () -> Unit,
    onNextQuestion: () -> Unit,
    onTogglePause: () -> Unit,
    onFinishQuiz: () -> Unit,
    onDismissSubmitError: () -> Unit,
    onDismissExitDialog: () -> Unit,
    onConfirmExit: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 80) }

    LaunchedEffect(currentQuestionIndex, selectedAnswer) {
        selectedOptionIndex = if (selectedAnswer in 0..3) selectedAnswer else null
        isAnswered = selectedOptionIndex != null
    }

    // Theme Colors
    val bgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBgColor = MaterialTheme.colorScheme.surface
    val optionDefaultBgColor = MaterialTheme.colorScheme.surfaceContainer
    val optionDefaultBorderColor = MaterialTheme.colorScheme.outlineVariant
    val progressColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val navBarColor = MaterialTheme.colorScheme.surface

    Surface(
        modifier = modifier.fillMaxSize(),
        color = bgColor
    ) {
        when (uiState) {
            is QuizUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = progressColor)
                }
            }

            is QuizUiState.Error -> {
                CenteredErrorDisplay(
                    message = uiState.message,
                    onRetry = onRetry
                )
            }

            is QuizUiState.Success -> {
                val questions = uiState.questions

                if (questions.isNotEmpty() && currentQuestionIndex < questions.size) {
                    val currentQuestion = questions[currentQuestionIndex]
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Header: Title and Total Time
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Practice Quiz",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Clock",
                                        modifier = Modifier.size(24.dp),
                                        tint = textColor
                                    )
                                    Text(
                                        text = "Total Time: ${String.format("%02d:%02d", totalTimeLeft / 60, totalTimeLeft % 60)}",
                                        fontSize = 18.sp,
                                        color = textColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Progress Bar
                            LinearProgressIndicator(
                                progress = { (currentQuestionIndex + 1).toFloat() / questions.size },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp),
                                color = progressColor,
                                trackColor = trackColor
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Question Info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Question ${currentQuestionIndex + 1}/${questions.size}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor
                                )

                                Text(
                                    text = "${questionTimeLeft}s",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (questionTimeLeft <= 10) QuizWrong else textColor
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Question Text (English and Marathi)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = cardBgColor
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = questions[currentQuestionIndex].questionEnglish,
                                        fontSize = 16.sp,
                                        color = textColor
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = questions[currentQuestionIndex].questionMarathi,
                                        fontSize = 16.sp,
                                        color = textColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Options
                            currentQuestion.optionsEnglish.zip(currentQuestion.optionsMarathi)
                                .forEachIndexed { index, (optionEnglish, optionMarathi) ->
                                    if (optionEnglish.isNotBlank() || optionMarathi.isNotBlank()) {
                                        AnswerOption(
                                            optionIndex = index,
                                            optionEnglish = optionEnglish,
                                            optionMarathi = optionMarathi,
                                            correctOptionIndex = currentQuestion.correctOptionIndex,
                                            selectedOptionIndex = selectedOptionIndex,
                                            isAnswered = isAnswered,
                                            defaultBgColor = optionDefaultBgColor,
                                            defaultBorderColor = optionDefaultBorderColor,
                                            textColor = textColor,
                                            subTextColor = subTextColor,
                                            onSelect = {
                                                if (isAnswered) return@AnswerOption
                                                selectedOptionIndex = index
                                                isAnswered = true
                                                onOptionSelected(index)
                                                val isCorrect = index == currentQuestion.correctOptionIndex
                                                if (isCorrect) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
                                                } else {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 180)
                                                }
                                            }
                                        )
                                    }
                                }

                            Spacer(modifier = Modifier.height(80.dp)) // Add extra space for navigation buttons
                        }

                        // Navigation Buttons fixed at bottom
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(navBarColor)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = onPreviousQuestion,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).padding(end = 4.dp),
                                enabled = currentQuestionIndex > 0
                            ) {
                                Text("Previous")
                            }

                            Button(
                                onClick = onTogglePause,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                            ) {
                                Text(if (isPaused) "Resume" else "Pause", color = MaterialTheme.colorScheme.onTertiary)
                            }

                            Button(
                                onClick = {
                                    if (currentQuestionIndex < questions.size - 1) {
                                        onNextQuestion()
                                    } else {
                                        onFinishQuiz()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).padding(start = 4.dp)
                            ) {
                                Text(if (currentQuestionIndex < questions.size - 1) "Next" else "Finish")
                            }
                        }

                        if (isSubmitting) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Unknown state encountered", color = textColor)
                }
            }
        }
    }

    if (submitError != null) {
        AlertDialog(
            onDismissRequest = onDismissSubmitError,
            title = { Text("Submission failed") },
            text = { Text(submitError) },
            confirmButton = {
                TextButton(onClick = onDismissSubmitError) {
                    Text("OK")
                }
            }
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = onDismissExitDialog,
            title = { Text("Quit Exam") },
            text = { Text("Do you want to quit the exam?") },
            confirmButton = {
                TextButton(onClick = onConfirmExit) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissExitDialog) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
private fun AnswerOption(
    optionIndex: Int,
    optionEnglish: String,
    optionMarathi: String,
    correctOptionIndex: Int,
    selectedOptionIndex: Int?,
    isAnswered: Boolean,
    defaultBgColor: Color,
    defaultBorderColor: Color,
    textColor: Color,
    subTextColor: Color,
    onSelect: () -> Unit
) {
    val isCorrect = optionIndex == correctOptionIndex
    val isSelected = selectedOptionIndex == optionIndex

    val backgroundColor = when {
        isAnswered && isCorrect -> QuizCorrect.copy(alpha = 0.1f)
        isAnswered && isSelected && !isCorrect -> QuizWrong.copy(alpha = 0.1f)
        else -> defaultBgColor
    }

    val borderColor = when {
        isAnswered && isCorrect -> QuizCorrect
        isAnswered && isSelected && !isCorrect -> QuizWrong
        else -> defaultBorderColor
    }

    val borderWidth = if (isSelected) 3.dp else 1.dp
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(enabled = !isAnswered) { onSelect() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${optionIndex + 1}.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(end = 12.dp),
                color = textColor
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(optionEnglish, fontWeight = FontWeight.Medium, color = textColor)
                Spacer(modifier = Modifier.height(4.dp))
                Text(optionMarathi, fontSize = 13.sp, color = subTextColor)
            }

            if (isAnswered) {
                when {
                    isCorrect -> Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Correct",
                        tint = QuizCorrect
                    )
                    isSelected && !isCorrect -> Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Wrong",
                        tint = QuizWrong
                    )
                }
            }
        }
    }
}

@Preview(name = "Light Mode")
@Composable
fun PracticeQuizScreenPreview() {
    val dummyQuestion = Question(
        id = "1",
        questionEnglish = "What is the capital of India?",
        questionMarathi = "भारताची राजधानी कोणती?",
        optionsEnglish = listOf("Mumbai", "New Delhi", "Kolkata", "Chennai"),
        optionsMarathi = listOf("मुंबई", "नवी दिल्ली", "कोलकाता", "चेन्नई"),
        correctOptionIndex = 1,
        type = QuestionType.MCQ,
        explanation = null
    )
    val successState = QuizUiState.Success(listOf(dummyQuestion))
    
    DISHAComputerTheme {
        PracticeQuizContent(
            uiState = successState,
            currentQuestionIndex = 0,
            selectedAnswer = -1,
            questionTimeLeft = 30,
            totalTimeLeft = 1800,
            isPaused = false,
            isSubmitting = false,
            submitError = null,
            showExitDialog = false,
            onOptionSelected = {},
            onPreviousQuestion = {},
            onNextQuestion = {},
            onTogglePause = {},
            onFinishQuiz = {},
            onDismissSubmitError = {},
            onDismissExitDialog = {},
            onConfirmExit = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Dark Mode")
@Composable
fun PracticeQuizScreenDarkPreview() {
    val dummyQuestion = Question(
        id = "1",
        questionEnglish = "What is the capital of India?",
        questionMarathi = "भारताची राजधानी कोणती?",
        optionsEnglish = listOf("Mumbai", "New Delhi", "Kolkata", "Chennai"),
        optionsMarathi = listOf("मुंबई", "नवी दिल्ली", "कोलकाता", "चेन्नई"),
        correctOptionIndex = 1,
        type = QuestionType.MCQ,
        explanation = null
    )
    val successState = QuizUiState.Success(listOf(dummyQuestion))
    
    DISHAComputerTheme(darkTheme = true) {
        PracticeQuizContent(
            uiState = successState,
            currentQuestionIndex = 0,
            selectedAnswer = -1,
            questionTimeLeft = 30,
            totalTimeLeft = 1800,
            isPaused = false,
            isSubmitting = false,
            submitError = null,
            showExitDialog = false,
            onOptionSelected = {},
            onPreviousQuestion = {},
            onNextQuestion = {},
            onTogglePause = {},
            onFinishQuiz = {},
            onDismissSubmitError = {},
            onDismissExitDialog = {},
            onConfirmExit = {},
            onRetry = {}
        )
    }
}
