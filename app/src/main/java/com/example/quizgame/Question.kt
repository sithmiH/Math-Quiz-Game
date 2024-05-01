package com.example.quizgame

data class Question(
    val question: String,
    val answers: List<String>,
    val correctAnswer: Int
)