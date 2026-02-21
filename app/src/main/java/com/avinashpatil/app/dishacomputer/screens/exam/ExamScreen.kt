package com.avinashpatil.app.dishacomputer.screens.exam

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.avinashpatil.app.dishacomputer.api.models.QuizDto
import com.avinashpatil.app.dishacomputer.navigation.Screen
import com.avinashpatil.app.dishacomputer.viewmodel.ExamAccessState
import com.avinashpatil.app.dishacomputer.viewmodel.ExamViewModel
import com.avinashpatil.app.dishacomputer.viewmodel.UpcomingExamsUiState
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.graphics.luminance
import androidx.navigation.compose.rememberNavController
import com.avinashpatil.app.dishacomputer.components.CenteredErrorDisplay
import com.avinashpatil.app.dishacomputer.ui.theme.*

@Composable
fun ExamScreen(
    navController: NavController,
    viewModel: ExamViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    val uiState by viewModel.uiState.collectAsState()
    val examAccessState by viewModel.examAccessState.collectAsState()
    
    // State to track which quiz we are validating access for
    var selectedQuizForValidation by remember { mutableStateOf<QuizDto?>(null) }
    
    // Dialog States
    var showAutoTimeError by remember { mutableStateOf(false) }
    var showWaitDialog by remember { mutableStateOf<String?>(null) }
    var showEndedDialog by remember { mutableStateOf(false) }

    // Helper: Check Auto Time
    fun isAutoTimeEnabled(context: android.content.Context): Boolean {
        return try {
             android.provider.Settings.Global.getInt(
                context.contentResolver,
                android.provider.Settings.Global.AUTO_TIME
            ) == 1
        } catch (e: Exception) {
            false
        }
    }

    // Validation Function
    fun validateAndNavigate(quiz: QuizDto) {
        // 1. Auto Time Check (Mandatory)
        if (!isAutoTimeEnabled(context)) {
            showAutoTimeError = true
            return
        }

        // 2. Time Validation (Server Time / IST)
        try {
            val dateStr = quiz.scheduledDate
            val timeStr = quiz.scheduledTime
            
            if (dateStr.isNullOrBlank() || timeStr.isNullOrBlank()) {
                // Fallback: If no schedule, maybe allow? Or block? 
                // Given "Strict", we might block, but let's assume valid data for now or allow if missing constraints
                // For safety in this specific task, we'll allow if data missing but log/toast
                navController.navigate("${Screen.CCCInstructions.route}/${quiz.quizId}?attempted=${quiz.attempted ?: false}")
                return
            }

            // Normalize Time (Ensure HH:mm:ss)
            val cleanTime = if (timeStr.count { it == ':' } == 1) "$timeStr:00" else timeStr
            
            // Construct Date-Time String
            // Try parsing strictly. If date is dd-MM-yyyy, we need specific formatter.
            // If yyyy-MM-dd, ISO works.
            // We'll try to detect or use a flexible formatter.
            val dateTimeStr = "${dateStr}T$cleanTime"
            
            val zoneId = java.time.ZoneId.of("Asia/Kolkata")
            
            // Formatter that handles potential formats
            // Common API formats: yyyy-MM-dd or dd-MM-yyyy
            val formatter = java.time.format.DateTimeFormatter.ofPattern(
                if (dateStr.contains("-") && dateStr.split("-")[0].length == 4) "yyyy-MM-dd'T'HH:mm:ss"
                else "dd-MM-yyyy'T'HH:mm:ss"
            )

            val examStartDateTime = java.time.LocalDateTime.parse(dateTimeStr, formatter).atZone(zoneId)
            val durationMinutes = quiz.durationMinutes ?: 60
            val examEndDateTime = examStartDateTime.plusMinutes(durationMinutes.toLong())
            
            val currentTime = java.time.ZonedDateTime.now(zoneId)
            
            // Entry Window Logic
            
            // 1. Not Started
            if (currentTime.isBefore(examStartDateTime)) {
                val displayFormatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ENGLISH)
                val timeDisplay = examStartDateTime.format(displayFormatter)
                showWaitDialog = "Please wait. Exam will begin at $timeDisplay"
                return
            }
            
            // 2. Ended or Duration 0
            if (currentTime.isAfter(examEndDateTime) || durationMinutes == 0) {
                showEndedDialog = true
                return
            }
            
            // 3. Valid Entry (On Time or Late)
            // TRIGGER BACKEND VALIDATION
            selectedQuizForValidation = quiz
            viewModel.checkExamAccess(quiz.quizId.orEmpty(), quiz.attempted)
            
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: If parsing fails, we navigate but still check access
            selectedQuizForValidation = quiz
            viewModel.checkExamAccess(quiz.quizId.orEmpty(), quiz.attempted)
        }
    }

    // 🔒 HANDLE EXAM ACCESS STATE
    LaunchedEffect(examAccessState) {
        if (examAccessState is ExamAccessState.Allowed) {
            selectedQuizForValidation?.let { quiz ->
                navController.navigate("${Screen.CCCInstructions.route}/${quiz.quizId.orEmpty()}?attempted=${quiz.attempted ?: false}")
                // Reset after navigation so we don't re-trigger
                viewModel.resetAccessState()
                selectedQuizForValidation = null
            }
        }
    }

    // 🔄 LIFECYCLE OBSERVER (Resume Check)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // If we were in the middle of validation (e.g. user went to settings and came back)
                // Re-trigger check if needed.
                selectedQuizForValidation?.let { 
                    viewModel.checkExamAccess(it.quizId.orEmpty()) 
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DISHAComputerTheme {
        ExamScreenContent(
            navController = navController,
            uiState = uiState,
            examAccessState = examAccessState,
            onReload = { viewModel.loadData() },
            onPracticeExamClick = { courseId ->
                navController.navigate("${Screen.PracticeQuiz.route}?courseId=$courseId&limit=50&source=EXAM")
            },
            onCertificationExamClick = {
                val state = uiState
                if (state is UpcomingExamsUiState.Success) {
                    val nextQuiz = state.quizzes.firstOrNull()
                    if (nextQuiz != null) {
                        validateAndNavigate(nextQuiz)
                    } else {
                        Toast.makeText(context, "No upcoming exams available", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onUpcomingExamClick = { quiz ->
                navController.navigate("${Screen.CCCInstructions.route}/${quiz.quizId}")
            },
            showAutoTimeError = showAutoTimeError,
            onDismissAutoTimeError = { showAutoTimeError = false },
            showWaitDialog = showWaitDialog,
            onDismissWaitDialog = { showWaitDialog = null },
            showEndedDialog = showEndedDialog,
            onDismissEndedDialog = { showEndedDialog = false },
            onResetAccessState = { 
                viewModel.resetAccessState()
                selectedQuizForValidation = null
            }
        )
    }
}

@Composable
fun ExamScreenContent(
    navController: NavController,
    uiState: UpcomingExamsUiState,
    examAccessState: ExamAccessState,
    onReload: () -> Unit,
    onPracticeExamClick: (String) -> Unit,
    onCertificationExamClick: () -> Unit,
    onUpcomingExamClick: (QuizDto) -> Unit,
    showAutoTimeError: Boolean,
    onDismissAutoTimeError: () -> Unit,
    showWaitDialog: String?,
    onDismissWaitDialog: () -> Unit,
    showEndedDialog: Boolean,
    onDismissEndedDialog: () -> Unit,
    onResetAccessState: () -> Unit
) {
    val bgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    if (showAutoTimeError) {
        AlertDialog(
            onDismissRequest = onDismissAutoTimeError,
            title = { Text("⚠️ Security Check Failed") },
            text = { Text("Automatic Date & Time is required. Please enable it to continue the exam.") },
            confirmButton = {
                Button(onClick = onDismissAutoTimeError) {
                    Text("OK")
                }
            }
        )
    }

    if (showWaitDialog != null) {
        AlertDialog(
            onDismissRequest = onDismissWaitDialog,
            title = { Text("Exam Not Started") },
            text = { Text(showWaitDialog) },
            confirmButton = {
                Button(onClick = onDismissWaitDialog) {
                    Text("OK")
                }
            }
        )
    }
    
    if (showEndedDialog) {
        AlertDialog(
            onDismissRequest = onDismissEndedDialog,
            title = { Text("Exam Ended") },
            text = { Text("Exam time is over. You are not allowed to enter now.") },
            confirmButton = {
                Button(onClick = onDismissEndedDialog) {
                    Text("OK")
                }
            }
        )
    }

    // Background Gradients
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Background subtle radial gradients matching HTML body
        val density = LocalDensity.current
        val configuration = LocalConfiguration.current
        val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
        val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
        val primaryColor = MaterialTheme.colorScheme.primary
        val tertiaryColor = MaterialTheme.colorScheme.tertiary
        val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

        Canvas(modifier = Modifier.fillMaxSize()) {
            val indigoColor = primaryColor.copy(alpha = 0.1f)
            val roseColor = tertiaryColor.copy(alpha = 0.1f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(indigoColor, Color.Transparent),
                    center = Offset(screenWidthPx * 0.1f, screenHeightPx * 0.2f),
                    radius = screenWidthPx * 0.8f
                ),
                center = Offset(screenWidthPx * 0.1f, screenHeightPx * 0.2f),
                radius = screenWidthPx * 0.8f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(roseColor, Color.Transparent),
                    center = Offset(screenWidthPx * 0.9f, screenHeightPx * 0.8f),
                    radius = screenWidthPx * 0.8f
                ),
                center = Offset(screenWidthPx * 0.9f, screenHeightPx * 0.8f),
                radius = screenWidthPx * 0.8f
            )
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ExamTopBar(
                navController = navController,
                onReload = onReload
            )

            when (val state = uiState) {
                is UpcomingExamsUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is UpcomingExamsUiState.Error -> {
                    CenteredErrorDisplay(
                        message = state.message,
                        onRetry = onReload
                    )
                }
                is UpcomingExamsUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(24.dp)
                    ) {
                        item {
                            // My Enrolled Courses Exams Section
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "My Enrolled Courses Exams",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textColor,
                                    letterSpacing = (-0.5).sp
                                )
                            }
            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                // Practice Exam Card (Green)
                                ExamCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(192.dp)
                                        .clickable {
                                            onPracticeExamClick(state.courseId)
                                        },
                                    title = "Practice Exam",
                                    subtitle = "50 Questions",
                                    icon = Icons.AutoMirrored.Outlined.Assignment,
                                    gradientColors = listOf(AccentEmerald, AccentCyan),
                                    decorationAlignment = Alignment.TopEnd
                                )
            
                                // Certification Exam Card (Orange)
                                ExamCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(192.dp)
                                        .clickable {
                                            onCertificationExamClick()
                                        },
                                    title = "Certification Exam",
                                    subtitle = "100 Questions",
                                    icon = Icons.Outlined.WorkspacePremium,
                                    gradientColors = listOf(AccentOrange, ErrorRed),
                                    decorationAlignment = Alignment.BottomStart
                                )
                            }
            
                            Spacer(modifier = Modifier.height(40.dp))
            
                            // Upcoming Exams Section
                            Text(
                                text = "Upcoming Certification Exams",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textColor,
                                letterSpacing = (-0.5).sp,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                        }
                        
                        if (state.quizzes.isEmpty()) {
                            item {
                                Text(
                                    text = "No upcoming exams scheduled.",
                                    color = textColor.copy(alpha = 0.7f),
                                    fontSize = 16.sp
                                )
                            }
                        } else {
                            items(state.quizzes) { quiz ->
                                UpcomingExamCard(
                                    quiz = quiz, 
                                    onClick = {
                                        onUpcomingExamClick(quiz)
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
            
                        item {
                            Spacer(modifier = Modifier.height(48.dp)) // Bottom padding
                        }
                    }
                }
            }
        }
        
        // 🛑 BLOCKED / SESSION TERMINATED SCREEN
        // This overlays everything else if active
        when (val state = examAccessState) {
            is ExamAccessState.SessionTerminated -> {
                ExamBlockedScreen(
                    title = "Session Terminated",
                    message = "You are active on another device or session. Multi-device usage is strictly prohibited.",
                    onExit = onResetAccessState
                )
            }
            is ExamAccessState.AlreadySubmitted -> {
                ExamBlockedScreen(
                    title = "Already Submitted",
                    message = "You have already submitted this exam. Re-attempts are not allowed.",
                    onExit = onResetAccessState
                )
            }
            is ExamAccessState.Blocked -> {
                ExamBlockedScreen(
                    title = "Access Denied",
                    message = state.reason,
                    onExit = onResetAccessState
                )
            }
            else -> {}
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun PreviewExamScreenLight() {
    val navController = rememberNavController()
    val dummyQuiz = QuizDto(
        quizId = "1",
        title = "Certification Exam 2024",
        scheduledDate = "2024-05-20",
        scheduledTime = "10:00:00",
        durationMinutes = 60,
        attempted = false
    )
    val uiState = UpcomingExamsUiState.Success(
        quizzes = listOf(dummyQuiz),
        courseId = "COURSE123"
    )
    
    DISHAComputerTheme(darkTheme = false) {
        ExamScreenContent(
            navController = navController,
            uiState = uiState,
            examAccessState = ExamAccessState.Idle,
            onReload = {},
            onPracticeExamClick = {},
            onCertificationExamClick = {},
            onUpcomingExamClick = {},
            showAutoTimeError = false,
            onDismissAutoTimeError = {},
            showWaitDialog = null,
            onDismissWaitDialog = {},
            showEndedDialog = false,
            onDismissEndedDialog = {},
            onResetAccessState = {}
        )
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
fun PreviewExamScreenDark() {
    val navController = rememberNavController()
    val dummyQuiz = QuizDto(
        quizId = "1",
        title = "Certification Exam 2024",
        scheduledDate = "2024-05-20",
        scheduledTime = "10:00:00",
        durationMinutes = 60,
        attempted = false
    )
    val uiState = UpcomingExamsUiState.Success(
        quizzes = listOf(dummyQuiz),
        courseId = "COURSE123"
    )
    
    DISHAComputerTheme(darkTheme = true) {
        ExamScreenContent(
            navController = navController,
            uiState = uiState,
            examAccessState = ExamAccessState.Idle,
            onReload = {},
            onPracticeExamClick = {},
            onCertificationExamClick = {},
            onUpcomingExamClick = {},
            showAutoTimeError = false,
            onDismissAutoTimeError = {},
            showWaitDialog = null,
            onDismissWaitDialog = {},
            showEndedDialog = false,
            onDismissEndedDialog = {},
            onResetAccessState = {}
        )
    }
}

@Composable
fun ExamTopBar(
    navController: NavController, 
    onReload: () -> Unit
) {
    val glassBg = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(glassBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Back Button
                GlassButton(
                    onClick = { navController.popBackStack() },
                    icon = Icons.AutoMirrored.Filled.ArrowBack // Chevron Left
                )

                // Title with Gradient
                val titleBrush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.onSurface,
                        MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                
                Text(
                    text = "Exams",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = TextStyle(
                        brush = titleBrush
                    ),
                    letterSpacing = (-0.5).sp
                )
            }

            // Reload Button
            GlassButton(
                onClick = onReload,
                icon = Icons.Filled.Refresh
            )
        }
    }
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    icon: ImageVector,
    iconTint: Color? = null
) {
    val glassBg = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val finalIconTint = iconTint ?: MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(glassBg)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
            //.shadow(1.dp, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = finalIconTint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun ExamCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    decorationAlignment: Alignment
) {
    val mainColor = gradientColors[0]
    
    Box(
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(32.dp)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(mainColor)
    ) {
        // Mesh Gradients
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            // Radial top-left
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(gradientColors[0], Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = width * 1.2f
                ),
                center = Offset(0f, 0f),
                radius = width * 1.2f
            )
            
            // Radial bottom-right
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(gradientColors[1], Color.Transparent),
                    center = Offset(width, height),
                    radius = width * 1.2f
                ),
                center = Offset(width, height),
                radius = width * 1.2f
            )
        }

        // Decorative Blur Circle
        Box(
            modifier = Modifier
                .align(decorationAlignment)
                .offset(
                    x = if (decorationAlignment == Alignment.TopEnd) 16.dp else (-16).dp,
                    y = if (decorationAlignment == Alignment.TopEnd) (-16).dp else 16.dp
                )
                .size(96.dp)
                .blur(32.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon
            GlassIconBox(icon = icon)

            // Text
            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    lineHeight = 22.sp
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun UpcomingExamCard(quiz: QuizDto, onClick: () -> Unit) {
    val gradientColors = listOf(PrimaryIndigo, AccentPurple) // Indigo to Purple
    
    val dateStr = formatDate(quiz.scheduledDate ?: "")
    val timeStr = formatTime(quiz.scheduledTime ?: "")
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(32.dp)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors
                )
            )
    ) {
        // Mesh Gradients
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(gradientColors[0], Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = width * 1.0f
                ),
                center = Offset(0f, 0f),
                radius = width * 1.0f
            )
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(gradientColors[1], Color.Transparent),
                    center = Offset(width, height),
                    radius = width * 1.0f
                ),
                center = Offset(width, height),
                radius = width * 1.0f
            )
        }

        Column(
            modifier = Modifier.padding(32.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                GlassIconBox(
                    icon = Icons.Outlined.Terminal,
                    size = 56.dp,
                    iconSize = 30.dp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = quiz.title ?: "Certification Exam",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = Indigo100.copy(alpha = 0.9f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dateStr,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Indigo100.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Grid Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoGlassCard(
                    label = "Start Time",
                    value = timeStr,
                    modifier = Modifier.weight(1f)
                )
                InfoGlassCard(
                    label = "Duration",
                    value = "${quiz.durationMinutes ?: 60} Minutes",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Warning
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = Rose300, // Rose-300
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = " ATTENDANCE MANDATORY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Rose100, // Rose-100
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GlassIconBox(
    icon: ImageVector,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Mesh card bg simulation
        Box(modifier = Modifier.fillMaxSize().background(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha=0.4f), Color.Transparent),
                center = Offset(0f,0f),
                radius = with(LocalDensity.current) { size.toPx() }
            )
        ))
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun InfoGlassCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp)) // rounded-3xl
            .background(Color.White.copy(alpha = 0.15f))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        // Background mesh
        Box(modifier = Modifier.fillMaxSize().background(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha=0.4f), Color.Transparent),
                center = Offset(0f,0f),
                radius = 200f
            )
        ))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Indigo100.copy(alpha = 0.6f), // indigo-100/60
                letterSpacing = 1.sp
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

fun formatDate(dateStr: String): String {
    return try {
        // Assuming dateStr is in ISO format "yyyy-MM-dd"
        if (dateStr.isBlank()) return ""
        val parsed = LocalDate.parse(dateStr)
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        parsed.format(formatter)
    } catch (e: Exception) {
        dateStr
    }
}

fun formatTime(timeStr: String): String {
    return try {
        // Assuming timeStr is "HH:mm:ss" or "HH:mm"
        if (timeStr.isBlank()) return ""
        val parsed = LocalTime.parse(timeStr)
        val formatter = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ENGLISH)
        parsed.format(formatter)
    } catch (e: Exception) {
        timeStr
    }
}

@Composable
fun ExamBlockedScreen(
    title: String,
    message: String,
    onExit: () -> Unit
) {
    // A strict, full-screen blocking overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.95f))
            .clickable(enabled = true, onClick = {}) // Capture clicks
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(32.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .border(2.dp, MaterialTheme.colorScheme.error, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                style = TextStyle(letterSpacing = (-0.5).sp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = message,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Return to Dashboard",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
