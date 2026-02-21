package com.avinashpatil.app.dishacomputer.screens.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.avinashpatil.app.dishacomputer.navigation.Screen
import com.avinashpatil.app.dishacomputer.viewmodel.FinalExamState
import com.avinashpatil.app.dishacomputer.viewmodel.QuizViewModel
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import android.widget.Toast
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.avinashpatil.app.dishacomputer.components.CenteredErrorDisplay
import com.avinashpatil.app.dishacomputer.ui.theme.DISHAComputerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    navController: NavController,
    viewModel: QuizViewModel = hiltViewModel(),
    courseId: String? = null,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    var finalExamClickRequested by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val finalExamState by viewModel.finalExamState.collectAsState()

    LaunchedEffect(courseId) {
        visible = true
        viewModel.loadFinalExam()
    }

    LaunchedEffect(finalExamState, finalExamClickRequested) {
        if (!finalExamClickRequested) return@LaunchedEffect

        when (val state = finalExamState) {
            is FinalExamState.Available -> {
                finalExamClickRequested = false
                navController.navigate("${Screen.FinalExam.route}/${state.quiz.quizId}") {
                    launchSingleTop = true
                }
            }
            is FinalExamState.NotScheduled -> {
                finalExamClickRequested = false
                Toast.makeText(
                    context,
                    "The certification exam has not been scheduled yet. You will be notified once it is scheduled.",
                    Toast.LENGTH_LONG
                ).show()
            }
            is FinalExamState.NotEligible -> {
                finalExamClickRequested = false
                Toast.makeText(
                    context,
                    "You are not eligible for this final exam.",
                    Toast.LENGTH_LONG
                ).show()
            }
            is FinalExamState.Error -> {
                finalExamClickRequested = false
                // Error is displayed via CenteredErrorDisplay in the UI
            }
            is FinalExamState.Loading -> Unit
        }
    }

    fun downloadCertificate() {
        // In a real app, this would download the certificate file
        // For demo purposes, we'll just show a toast
        android.widget.Toast.makeText(
            context,
            "Downloading certificate...",
            android.widget.Toast.LENGTH_SHORT
        ).show()

        // Actual implementation might look like:
        // 1. Check storage permissions
        // 2. Download file from server
        // 3. Save to downloads folder
        // 4. Show success/failure message
    }

    fun shareCertificate() {
        // In a real app, this would share the actual certificate file
        // For demo, we'll create a dummy file URI

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_TEXT, "Check out my Certificate!")
            putExtra(Intent.EXTRA_SUBJECT, "Certificate")

            // For actual file sharing:
            // val file = File("path_to_certificate.pdf")
            // val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            // putExtra(Intent.EXTRA_STREAM, uri)
            // flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(Intent.createChooser(intent, "Share Certificate"))
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .shadow(
                            8.dp,
                            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Text(
                            text = "EXAMS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        IconButton(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "My Enrolled Courses Exams",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clickable {
                                    val route = if (courseId.isNullOrBlank()) {
                                        "${Screen.PracticeQuiz.route}?limit=25&source=EXAM"
                                    } else {
                                        "${Screen.PracticeQuiz.route}?courseId=$courseId&limit=25&source=EXAM"
                                    }
                                    navController.navigate(route) { launchSingleTop = true }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Practice Quiz",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Practice Quiz",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "25 Questions",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }

                        if (finalExamState !is FinalExamState.NotEligible) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clickable {
                                        finalExamClickRequested = true
                                        viewModel.loadFinalExam()
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = "Final Exam",
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Final Exam",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = "100 Questions",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Upcoming Exams",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (finalExamState is FinalExamState.NotScheduled) {
                        Text(
                            text = "The certification exam has not been scheduled yet. You will be notified once it is scheduled.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (finalExamState is FinalExamState.NotEligible) {
                        Text(
                            text = "You are not eligible for this final exam.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    (finalExamState as? FinalExamState.Error)?.let { errorState ->
                        CenteredErrorDisplay(
                            message = errorState.message,
                            onRetry = { viewModel.loadFinalExam() },
                            modifier = Modifier.height(200.dp)
                        )
                    }

                    val quiz = (finalExamState as? FinalExamState.Available)?.quiz
                    quiz?.let { examQuiz ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    val quizId = examQuiz.quizId ?: ""
                                    if (quizId.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            "The certification exam has not been scheduled yet. You will be notified once it is scheduled.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        navController.navigate("${Screen.FinalExam.route}/$quizId") {
                                            launchSingleTop = true
                                        }
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            val (dateText, timeText) = examQuiz.scheduledTime?.let(::formatIsoDateTime)
                                ?: ("Not scheduled" to "")

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Certificate Exam",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Date: $dateText",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                if (timeText.isNotBlank()) {
                                    Text(
                                        text = "Time: $timeText",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                                Text(
                                    text = "Note :- Attendance is mandatory.",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, showSystemUi = true)
@Composable
fun QuizScreenPreview() {
    DISHAComputerTheme {
        QuizScreen(navController = rememberNavController())
    }
}

@Preview(name = "Dark Mode", showBackground = true, showSystemUi = true)
@Composable
fun QuizScreenDarkPreview() {
    DISHAComputerTheme(darkTheme = true) {
        QuizScreen(navController = rememberNavController())
    }
}

private fun formatIsoDateTime(iso: String): Pair<String, String> {
    return try {
        val instant = runCatching { Instant.parse(iso) }.getOrElse {
            runCatching { OffsetDateTime.parse(iso).toInstant() }.getOrElse {
                val local = LocalDateTime.parse(iso.removeSuffix("Z"), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                local.atZone(ZoneId.systemDefault()).toInstant()
            }
        }
        val zone = ZoneId.systemDefault()
        val date = DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(zone).format(instant)
        val time = DateTimeFormatter.ofPattern("hh:mm a").withZone(zone).format(instant)
        date to time
    } catch (_: Exception) {
        iso to ""
    }
}
