package com.example.quizgame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {
    private val _score = MutableLiveData<Int>()   //hold current score value
    val score: LiveData<Int>
        get() = _score

    private val _highScore = MutableLiveData<Int>()    //hold high score value
    val highScore: LiveData<Int>
        get() = _highScore

    init {
        _score.value = 0
        _highScore.value = 0
    }

    fun updateScore(newScore: Int) {
        _score.value = newScore
    }

    fun update_high_score(newHighScore: Int) {
        _highScore.value = newHighScore
    }
}