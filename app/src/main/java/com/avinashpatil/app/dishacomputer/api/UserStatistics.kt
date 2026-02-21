package com.avinashpatil.app.dishacomputer.api

data class UserStatistics(
    val totalQuizzes: Int,
    val averageScore: Double,
    val timeSpent: Long,
    val strengthAreas: List<String>,
    val weakAreas: List<String>
)