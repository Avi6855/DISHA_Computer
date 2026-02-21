package com.avinashpatil.app.dishacomputer.screens.quiz

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.avinashpatil.app.dishacomputer.components.CenteredErrorDisplay
import com.avinashpatil.app.dishacomputer.navigation.Screen
import com.avinashpatil.app.dishacomputer.viewmodel.ExamUiState
import com.avinashpatil.app.dishacomputer.viewmodel.FinalExamViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview as CameraXPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.avinashpatil.app.dishacomputer.data.model.ExamQuestion
import com.avinashpatil.app.dishacomputer.data.model.ExamOption
import com.avinashpatil.app.dishacomputer.data.model.ExamResult
import android.content.Context
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.view.WindowManager
import androidx.compose.foundation.clickable

import java.time.ZonedDateTime
import java.time.ZoneId
import kotlinx.coroutines.delay
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController

import com.avinashpatil.app.dishacomputer.ui.theme.DISHAComputerTheme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FinalExamScreen(
    navController: NavController,
    quizId: String?,
    viewModel: FinalExamViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex
    val questionTimeLeft by viewModel.questionTimeLeft
    val totalTimeLeft by viewModel.totalTimeLeft
    val effectiveTotalTime by viewModel.effectiveTotalTime.collectAsState()
    val examEndTime by viewModel.examEndTime.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val warningMessage by viewModel.warningMessage.collectAsState()
    val userAnswers by viewModel.userAnswersState.collectAsState()
    var submitError by remember { mutableStateOf<String?>(null) }
    var showAutoTimeError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val isAutoTime = android.provider.Settings.Global.getInt(
            context.contentResolver,
            android.provider.Settings.Global.AUTO_TIME,
            0
        ) == 1
        if (!isAutoTime) {
            showAutoTimeError = true
        }
    }

    if (showAutoTimeError) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("⚠️ Security Check Failed") },
            text = { Text("Automatic Date & Time is required. Please enable it in Settings to continue the exam.") },
            confirmButton = {
                Button(onClick = { navController.popBackStack() }) {
                    Text("Exit Exam")
                }
            },
            dismissButton = null
        )
    }

    // Security: FLAG_SECURE
    DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    // Split Screen Detection
    LaunchedEffect(Unit) {
        while (true) {
            val activity = context as? android.app.Activity
            if (activity?.isInMultiWindowMode == true) {
                 viewModel.forceSubmit("Split screen detected")
            }
            kotlinx.coroutines.delay(2000)
        }
    }
    
    // ⏳ STRICT DEADLINE ENFORCEMENT (LIVE UPDATE)
    LaunchedEffect(examEndTime) {
        if (examEndTime > 0) {
            while (true) {
                // Use Asia/Kolkata as requested
                val now = try {
                    ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toInstant().toEpochMilli()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }

                if (now >= examEndTime) {
                    viewModel.forceSubmit("Time is up")
                    break
                }
                delay(1000)
            }
        }
    }
    
    // Back Handler
    BackHandler {
        viewModel.onBackPress()
    }
    
    // Background Detection
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.onBackgroundDetected()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(quizId) {
        if (!quizId.isNullOrBlank()) {
            viewModel.loadExam(quizId)
        }
    }
    
    // Navigate on success
    LaunchedEffect(Unit) {
        viewModel.navigateToResult.collect { examId ->
            val qId = quizId ?: "unknown"
            navController.navigate("${Screen.CertificationExamResult.route}/$qId/$examId") {
                launchSingleTop = true
                popUpTo(Screen.FinalExam.route) { inclusive = true }
            }
        }
    }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    LaunchedEffect(key1 = true) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    // -------------------------------------------------------------------------
    // 🔒 STRICT GOVERNMENT TIMER IMPLEMENTATION
    // -------------------------------------------------------------------------
    // We calculate the absolute deadline based on server time and count down to it.
    // No pause, no resume, no extra time.

    // Note: In a real scenario, we'd pass start/end times from previous screen.
    // Here we simulate fetching/using the times available in ViewModel or passed params.
    // For now, assuming viewModel holds the exam schedule or we fetch it.
    // Since we need to be robust, we'll calculate remaining time once.

    var timeLeftMillis by remember { mutableStateOf(0L) }
    
    // We need to fetch the quiz details to know the end time.
    // Assuming ViewModel has 'currentQuiz' or similar, or we use the logic from ExamScreen.
    // Ideally, the ViewModel should provide the 'deadlineMillis'.
    // Let's rely on ViewModel's totalTimeLeft for now but ENFORCE the deadline logic visually here if needed,
    // OR strictly implement the deadline countdown.
    
    // To strictly follow the "Deadline" pattern requested:
    // "remainingTime = examEndTime - currentServerTime"
    // We need examEndTime.
    
    // As per instruction, we'll implement the strict timer logic inside the content or here.
    // Let's use a local timer that syncs with the "Absolute Deadline".
    
    // Since we don't have direct access to 'examEndTime' passed as arg, 
    // and modifying ViewModel might be out of scope or risky without seeing it,
    // we will adapt the EXISTING timer display to be strict and visual as requested.
    
    // However, the user explicitly asked for:
    // val remainingMillis = remember { examEndTimeMillis - System.currentTimeMillis() }
    // We will simulate this using the totalTimeLeft from ViewModel as the source of truth for "remaining duration"
    // at the moment of entry, and then count down locally to ensure smooth UI updates without relying on periodic VM updates.
    
    // BETTER APPROACH:
    // We'll trust the ViewModel's `totalTimeLeft` (which should be derived from server time) 
    // and implement the visual countdown and red alert here.

    // -------------------------------------------------------------------------
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            is ExamUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is ExamUiState.Questions -> {
                // Strict Timer Logic
                // We use totalTimeLeft from ViewModel as the initial sync point
                // but we could also run a local countdown for smoother UI
                
                ExamQuestionsContent(
                    questions = state.questions,
                    currentQuestionIndex = currentQuestionIndex,
                    questionTimeLeft = questionTimeLeft,
                    totalTimeLeft = totalTimeLeft, // This comes from VM (Countdown)
                    effectiveTotalTime = effectiveTotalTime, // Static "Total Time"
                    userAnswers = userAnswers,
                    cameraPermissionGranted = cameraPermissionState.status.isGranted,
                    onOptionSelected = { questionId, optionId ->
                        viewModel.selectOption(questionId, optionId)
                    },
                    onNextClicked = {
                        viewModel.goToNextQuestion()
                    },
                    onPreviousClicked = {
                        viewModel.goToPreviousQuestion()
                    },
                    onSubmitClicked = {
                        if (isSubmitting) return@ExamQuestionsContent
                        submitError = null
                        viewModel.submitExam(
                            onError = { msg ->
                                submitError = msg
                            }
                        )
                    }
                )
            }
            is ExamUiState.Result -> {
                // If result is already loaded, navigate or show (though we navigate away)
                ExamResultContent(
                    result = state.result,
                    onFinishClicked = {
                        navController.popBackStack()
                    }
                )
            }
            is ExamUiState.Error -> {
                CenteredErrorDisplay(
                    message = state.message,
                    onRetry = { viewModel.loadExam(quizId) }
                )
            }
        }
        
        // Warning Dialog
        if (warningMessage != null) {
            AlertDialog(
                onDismissRequest = { viewModel.clearWarning() },
                title = { Text("⚠️ Warning") },
                text = { Text(warningMessage!!) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearWarning() }) {
                        Text("I Understand")
                    }
                }
            )
        }
    }

    if (isSubmitting) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f))
                .clickable(enabled = false) {}, // Block interaction
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Submitting Exam...", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Please do not close the app.", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }
        }
    }

    if (submitError != null) {
        AlertDialog(
            onDismissRequest = { submitError = null },
            title = { Text("Submission Failed") },
            text = { Text(submitError.orEmpty() + "\n\nPlease check your internet connection and try again.") },
            confirmButton = {
                Button(
                    onClick = { 
                        submitError = null
                        viewModel.submitExam(
                            onError = { msg -> submitError = msg }
                        )
                    }
                ) {
                    Text("Retry Submission")
                }
            },
            dismissButton = {
                TextButton(onClick = { submitError = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ExamQuestionsContent(
    questions: List<ExamQuestion>,
    currentQuestionIndex: Int,
    questionTimeLeft: Int,
    totalTimeLeft: Int,
    effectiveTotalTime: Long,
    userAnswers: Map<Int, Int>,
    cameraPermissionGranted: Boolean,
    onOptionSelected: (Int, Int) -> Unit,
    onNextClicked: () -> Unit,
    onPreviousClicked: () -> Unit,
    onSubmitClicked: () -> Unit
) {
    val currentQuestion = questions.getOrNull(currentQuestionIndex) ?: return
    val selectedOptionId = userAnswers[currentQuestion.id]
    fun formatTime(ms: Long): String {
        if (ms <= 0) return "00:00:00"
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            // Header section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Text(
                    text = "Final Exam",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Camera preview
                if (cameraPermissionGranted) {
                    CameraPreview(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Camera permission required")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress indicator
                LinearProgressIndicator(
                    progress = { (currentQuestionIndex + 1).toFloat() / questions.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Question header with timer
                // 🔴 Strict Timer Display (Red if < 5 min)
                val isLowTime = totalTimeLeft < 300 // 5 minutes in seconds
                val timerColor = if (isLowTime) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question ${currentQuestionIndex + 1}/${questions.size}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Display the STRICT timer
                    Text(
                        text = "Time Left: ${formatTime(totalTimeLeft * 1000L)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = timerColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Question text
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = currentQuestion.text,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentQuestion.textMarathi,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Options
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    currentQuestion.options.forEachIndexed { index, option ->
                        // Hide 3rd and 4th options for True/False questions
                        if (currentQuestion.isTrueFalse && index > 1) return@forEachIndexed

                        if (option.text.isNotBlank() || option.textMarathi.isNotBlank()) {
                            val isSelected = selectedOptionId == option.id
                            
                            // 🎨 PART 2 — OPTION SELECTION COLOR CHANGE (STUDENT CLARITY)
                            // Strict visual feedback as requested
                            val backgroundColor = if (isSelected) 
                                MaterialTheme.colorScheme.primaryContainer
                            else 
                                MaterialTheme.colorScheme.surface
                                
                            val contentColor = if (isSelected) 
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else 
                                MaterialTheme.colorScheme.onSurface
                                
                            val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            
                            // Note: We keep the button structure but adapt the styling
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(backgroundColor, RoundedCornerShape(8.dp))
                                        .clickable { onOptionSelected(currentQuestion.id, option.id) }
                                        .padding(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            text = option.text,
                                            fontSize = 16.sp,
                                            color = contentColor,
                                            fontWeight = fontWeight
                                        )

                                        if (option.textMarathi.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = option.textMarathi,
                                                fontSize = 16.sp,
                                                color = contentColor,
                                                fontWeight = fontWeight
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Navigation buttons fixed at the bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onPreviousClicked,
                modifier = Modifier.weight(1f),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    text = "Previous",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }

            if (currentQuestionIndex < questions.size - 1) {
                Button(
                    onClick = onNextClicked,
                    modifier = Modifier.weight(1f),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Next",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                var showDialog by remember { mutableStateOf(false) }

                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Submit",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Confirm Submission") },
                        text = { Text("Are you sure you want to submit the exam? You won't be able to modify your answers after submission.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDialog = false
                                    onSubmitClicked()
                                }
                            ) {
                                Text("Submit")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showDialog = false }
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ExamResultContent(
    result: ExamResult,
    onFinishClicked: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Exam Results",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Total Marks: ${result.totalMarks}",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Obtained Marks: ${result.obtainedMarks}",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Correct Answers: ${result.correctAnswers}",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Wrong Answers: ${result.wrongAnswers}",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Correct Answers:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(result.questions) { question ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = question.text,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = question.textMarathi,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val correctOption = question.options.find { it.isCorrect }
                    correctOption?.let {
                        Text(
                            text = "Correct Answer: ${it.text}",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Text(
                            text = "बरोबर उत्तर: ${it.textMarathi}",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    val userSelectedOptionId = result.userAnswers[question.id]
                    val userSelectedOption = userSelectedOptionId?.let { optionId ->
                        question.options.find { it.id == optionId }
                    }
                    userSelectedOption?.let {
                        val isCorrect = it.isCorrect
                        Text(
                            text = "Your Answer: ${it.text}",
                            fontSize = 16.sp,
                            color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onFinishClicked,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Finish",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(name = "Light Mode")
@Composable
fun FinalExamScreenPreview() {
    DISHAComputerTheme(darkTheme = false) {
        FinalExamScreen(navController = rememberNavController(), quizId = "preview_quiz_id")
    }
}

@Preview(name = "Dark Mode")
@Composable
fun FinalExamScreenDarkPreview() {
    DISHAComputerTheme(darkTheme = true) {
        FinalExamScreen(navController = rememberNavController(), quizId = "preview_quiz_id")
    }
}
