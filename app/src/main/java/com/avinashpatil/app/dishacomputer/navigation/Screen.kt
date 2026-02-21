package com.avinashpatil.app.dishacomputer.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object Exams : Screen("Exams")
    object PracticeQuiz : Screen("practice_quiz")
    object CCCInstructions : Screen("ccc_instructions")
    object FinalExam : Screen("final_exam")
    object QuizResult : Screen("quiz_result")
    object CertificationExamResult : Screen("certification_exam_result")
    object PracticeExamResult : Screen("practice_exam_result")
    object Rank : Screen("rank")
    //object Profile : Screen("profile")
    object ModernProfile : Screen("modern_profile")
    object DishaAI : Screen("DISHA")
    object ModernDishaAI : Screen("modern_disha_ai")
    object AllInOneBill : Screen("bill")
    object StudentDetails : Screen("student_details")
    object Topics : Screen("topics")
    object ExamScreen : Screen("exam_screen")
    object EnhancedHome : Screen("enhanced_home")
    object Restriction : Screen("restriction")

    companion object {
        val bottomNavItems = listOf(
            Home,
            Topics,
            //DishaAI,
            ModernDishaAI,
            Exams,
            ModernProfile
        )
    }
}