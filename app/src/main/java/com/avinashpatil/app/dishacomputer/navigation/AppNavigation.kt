package com.avinashpatil.app.dishacomputer.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.hilt.navigation.compose.hiltViewModel
import com.avinashpatil.app.dishacomputer.screens.bill.AllInOneBillScreen
import com.avinashpatil.app.dishacomputer.screens.bill.StudentDetailsScreen
import com.avinashpatil.app.dishacomputer.screens.dishaAI.ModernDishaAIScreen
import com.avinashpatil.app.dishacomputer.screens.exam.ExamScreen
import com.avinashpatil.app.dishacomputer.screens.home.EnhancedHomeScreen
import com.avinashpatil.app.dishacomputer.viewmodel.MainViewModel
import com.avinashpatil.app.dishacomputer.viewmodel.ModernProfileViewModel
import com.avinashpatil.app.dishacomputer.screens.quiz.FinalExamScreen
import com.avinashpatil.app.dishacomputer.screens.quiz.QuizScreen
import com.avinashpatil.app.dishacomputer.screens.splash.SplashScreen
import com.avinashpatil.app.dishacomputer.screens.ranking.RankingScreen
import com.avinashpatil.app.dishacomputer.screens.instructions.ModernInstructionsScreen
import com.avinashpatil.app.dishacomputer.screens.login.ModernAnimatedLoginForm
import com.avinashpatil.app.dishacomputer.screens.profile.ModernProfileScreen
import com.avinashpatil.app.dishacomputer.screens.result.PracticeExamResultScreen
import com.avinashpatil.app.dishacomputer.screens.quiz.PracticeQuizScreen
import com.avinashpatil.app.dishacomputer.screens.result.CertificationExamResultScreen
import com.avinashpatil.app.dishacomputer.screens.topic.TopicsScreen
import com.avinashpatil.app.dishacomputer.screens.lock_user.RestrictionScreen


@Composable
fun AppNavigation() {
    val mainViewModel: MainViewModel = hiltViewModel()
    val isLocked by mainViewModel.isLocked.collectAsState()
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Global Lock Enforcement
    LaunchedEffect(isLocked) {
        if (isLocked) {
            navController.navigate(Screen.Restriction.route) {
                // Pop everything up to the start destination to avoid back stack issues
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Show bottom bar only on main UI pages, hide on onboarding (Splash, Login)
    val showBottomBar = remember(currentRoute) {
        when {
            currentRoute == Screen.Splash.route -> false
            currentRoute == Screen.Login.route -> false
            currentRoute == Screen.Restriction.route -> false
            currentRoute?.startsWith(Screen.PracticeQuiz.route) == true -> false
                currentRoute == Screen.QuizResult.route -> false
                currentRoute?.startsWith(Screen.PracticeExamResult.route) == true -> false
                currentRoute?.startsWith(Screen.CCCInstructions.route) == true -> false
            currentRoute?.startsWith(Screen.CertificationExamResult.route) == true -> false
            currentRoute?.startsWith(Screen.FinalExam.route) == true -> false
            else -> true
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigation(
                navController = navController,
                visible = showBottomBar
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(navController)
            }

            composable(
                route = Screen.Login.route,
                enterTransition = { 
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                    )
                },
                exitTransition = { 
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                    )
                }
            ) {
                ModernAnimatedLoginForm(navController)
            }

            composable(Screen.Home.route) {
                EnhancedHomeScreen(
                    navController = navController
                    //onThemeChanged = { mainViewModel.setDarkTheme(it) }
                )
            }

            composable(
                route = Screen.Exams.route,
                deepLinks = listOf(
                    navDeepLink { uriPattern = "dishacomputer://quiz" }
                )
            ) {
                ExamScreen(navController)
            }

            composable(
                route = "${Screen.Exams.route}/{courseId}",
                arguments = listOf(
                    navArgument("courseId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId")
                QuizScreen(navController = navController, courseId = courseId)
            }

            composable(
                route = "${Screen.CCCInstructions.route}/{quizId}?attempted={attempted}",
                arguments = listOf(
                    navArgument("quizId") { type = NavType.StringType },
                    navArgument("attempted") { type = NavType.BoolType; defaultValue = false }
                ),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) { backStackEntry ->
                val quizId = backStackEntry.arguments?.getString("quizId")
                val attempted = backStackEntry.arguments?.getBoolean("attempted") ?: false
                ModernInstructionsScreen(
                    onStartExamClick = {
                        if (quizId != null) {
                            navController.navigate("${Screen.FinalExam.route}/$quizId")
                        }
                    },
                    attempted = attempted
                )
            }

            composable(
                route = "${Screen.FinalExam.route}/{quizId}",
                arguments = listOf(
                    navArgument("quizId") { type = NavType.StringType }
                ),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) { backStackEntry ->
                val quizId = backStackEntry.arguments?.getString("quizId")
                FinalExamScreen(navController = navController, quizId = quizId)
            }

            composable(
                route = "${Screen.CertificationExamResult.route}/{quizId}/{examId}",
                arguments = listOf(
                    navArgument("quizId") { type = NavType.StringType },
                    navArgument("examId") { type = NavType.StringType }
                ),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) { backStackEntry ->
                val quizId = backStackEntry.arguments?.getString("quizId") ?: ""
                val examId = backStackEntry.arguments?.getString("examId") ?: ""
                CertificationExamResultScreen(
                    navController = navController,
                    quizId = quizId,
                    examId = examId
                )
            }

            composable(
                route = "${Screen.PracticeQuiz.route}?courseId={courseId}&topicId={topicId}&limit={limit}&source={source}",
                arguments = listOf(
                    navArgument("courseId") { type = NavType.StringType; nullable = true },
                    navArgument("topicId") { type = NavType.StringType; nullable = true },
                    navArgument("limit") { type = NavType.IntType; defaultValue = 25 },
                    navArgument("source") { type = NavType.StringType; defaultValue = "TOPIC" }
                ),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId")
                val topicId = backStackEntry.arguments?.getString("topicId")
                val limit = backStackEntry.arguments?.getInt("limit") ?: 25
                val source = backStackEntry.arguments?.getString("source") ?: "TOPIC"
                
                PracticeQuizScreen(
                    navController = navController,
                    courseId = courseId,
                    topicId = topicId,
                    limit = limit,
                    source = source
                )
            }

            composable(
                route = "${Screen.PracticeExamResult.route}/{score}/{total}/{correct}/{wrong}/{skipped}/{topicId}?source={source}",
                arguments = listOf(
                    navArgument("score") { type = NavType.IntType },
                    navArgument("total") { type = NavType.IntType },
                    navArgument("correct") { type = NavType.IntType },
                    navArgument("wrong") { type = NavType.IntType },
                    navArgument("skipped") { type = NavType.IntType },
                    navArgument("topicId") { type = NavType.StringType },
                    navArgument("source") { type = NavType.StringType; defaultValue = "TOPIC" }
                ),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) { backStackEntry ->
                val score = backStackEntry.arguments?.getInt("score") ?: 0
                val total = backStackEntry.arguments?.getInt("total") ?: 0
                val correct = backStackEntry.arguments?.getInt("correct") ?: 0
                val wrong = backStackEntry.arguments?.getInt("wrong") ?: 0
                val skipped = backStackEntry.arguments?.getInt("skipped") ?: 0
                val topicId = backStackEntry.arguments?.getString("topicId") ?: ""
                val source = backStackEntry.arguments?.getString("source") ?: "TOPIC"
                
                PracticeExamResultScreen(
                    navController = navController,
                    score = score,
                    totalScore = total,
                    correctAnswers = correct,
                    wrongAnswers = wrong,
                    skippedAnswers = skipped,
                    topicId = topicId,
                    source = source
                )
            }

            composable(Screen.Rank.route) {
                RankingScreen(navController)
            }

            composable(Screen.Topics.route) {
                TopicsScreen(navController = navController)
            }

            composable(Screen.ModernProfile.route) {
                val viewModel: ModernProfileViewModel = hiltViewModel()
                ModernProfileScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }

            composable(Screen.StudentDetails.route) {
                StudentDetailsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = "${Screen.AllInOneBill.route}/{studentId}",
                arguments = listOf(
                    navArgument("studentId") {
                        type = NavType.StringType
                        defaultValue = "0"
                    }
                )
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId") ?: "0"
                AllInOneBillScreen(
                    navController = navController,
                    studentId = studentId
                )
            }

            composable(
                route = Screen.ModernDishaAI.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) {
                ModernDishaAIScreen()
            }

            composable(
                route = Screen.Restriction.route,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
            ) {
                RestrictionScreen(navController)
            }
        }
    }
}