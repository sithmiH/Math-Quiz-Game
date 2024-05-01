package com.example.quizgame

import android.os.Build
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import java.util.Random

class MainActivity : AppCompatActivity() {

    private val soundManager = GameSound(this)
    private lateinit var sharedPreferences: SharedPreferences
    private var highScore: Int = 0

    private var operation: Int = ADDITION  // Default to ADDITION


    companion object {
        const val ADDITION = 0
        const val SUBTRACTION = 1
        const val MULTIPLICATION = 2
        const val DIVISION = 3
        const val RANDOM = 4
    }

    private lateinit var startBtn: LinearLayout
    private lateinit var subtractBtn: LinearLayout
    private lateinit var multiplyBtn: LinearLayout
    private lateinit var divisionBtn: LinearLayout
    private lateinit var randomBtn: LinearLayout
    private lateinit var gamelayout: LinearLayout
    private lateinit var dashbord: RelativeLayout
    private lateinit var gameover: RelativeLayout
    private lateinit var questionView: TextView
    private lateinit var option1Btn: Button
    private lateinit var option2Btn: Button
    private lateinit var option3Btn: Button
    private lateinit var option4Btn: Button
    private lateinit var exitBtn: Button
    private lateinit var gotohome: Button
    private lateinit var finalscoretv: TextView
    private lateinit var timerTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var questionNumberView: Button
    private var currentQuestionNumber: Int = 1

    private val random = Random()
    private var score = 0
    private var currentQstIndex = 0
    private lateinit var currentQuestion: Question  //hold currently displayed question
    private var timer: CountDownTimer? = null

    private lateinit var scoreView: TextView
    private val correctColor: Int by lazy { ContextCompat.getColor(this, R.color.correctColor) }
    private val incorrectColor: Int by lazy { ContextCompat.getColor(this, R.color.incorrectColor) }

    private lateinit var viewModel: GameViewModel

    private lateinit var highScoreView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(GameViewModel::class.java)

        viewModel.score.observe(this, { score ->

            scoreView.text = "Score: $score"   // Update UI with new score
        })

        viewModel.highScore.observe(this, { highScore ->

            highScoreView.text = "High Score: $highScore"   // Update UI with new high score
        })

        sharedPreferences = getSharedPreferences("QuizGamePrefs", Context.MODE_PRIVATE)
        highScore = sharedPreferences.getInt("highScore", 0) //retrieves the high score from the shared preferences


        val exitLayout: LinearLayout = findViewById(R.id.exit)

        val exitButton : Button = findViewById(R.id.exitbutton)
        val gotohome : Button = findViewById(R.id.gotohome)

        highScoreView = findViewById(R.id.highScoreTextView)

        //display exit alert box
        val exitClickListener = View.OnClickListener {
            val builder = AlertDialog.Builder(this)

            builder.setTitle("Exit")
                .setMessage("Are you sure you want to exit the app?")

            builder.setPositiveButton("Yes") { dialog, which ->
                finish()
            }

            builder.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        exitLayout.setOnClickListener(exitClickListener)
        exitButton.setOnClickListener(exitClickListener)

        startBtn = findViewById(R.id.addition_layout)
        subtractBtn = findViewById(R.id.subtract_button)
        multiplyBtn = findViewById(R.id.multiply_button)
        divisionBtn = findViewById(R.id.division_button)
        randomBtn = findViewById(R.id.random_button)
        gamelayout = findViewById(R.id.gamelayout)
        dashbord = findViewById(R.id.dashboard)
        gameover = findViewById(R.id.gameoverscreen)
        progressBar = findViewById(R.id.progressBar)
        timerTextView = findViewById(R.id.timerTextView)
        scoreView = findViewById(R.id.scoreTextView)
        finalscoretv = findViewById(R.id.final_score)
        questionNumberView = findViewById(R.id.questionNumberTextView)

        gamelayout.visibility = View.GONE

        startBtn.setOnClickListener {

            val durationInMillis: Long = 10000
            val intervalInMillis: Long = 100

            object : CountDownTimer(durationInMillis, intervalInMillis) {

                //during each tick the progress bar is updated
                override fun onTick(millisUntilFinished: Long) {
                    val progress = (millisUntilFinished * 100 / durationInMillis).toInt()
                    progressBar.progress = progress

                    val secondsRemaining = (millisUntilFinished / 1000).toInt()
                    timerTextView.text = secondsRemaining.toString()
                }

                override fun onFinish() {
                    timerTextView.text = "0"  //display the remaining seconds
                }
            }.start()  //start countdown timer
        }

        questionView = findViewById(R.id.questionTextView)
        option1Btn = findViewById(R.id.option1Button)
        option2Btn = findViewById(R.id.option2Button)
        option3Btn = findViewById(R.id.option3Button)
        option4Btn = findViewById(R.id.option4Button)
        timerTextView = findViewById(R.id.timerTextView)
        progressBar = findViewById(R.id.progressBar)

        startBtn.setOnClickListener {
            // Default operation
            startGame(ADDITION)
        }

        subtractBtn.setOnClickListener {
            operation = SUBTRACTION
            startGame(SUBTRACTION)
        }

        multiplyBtn.setOnClickListener {
            operation = MULTIPLICATION
            startGame(MULTIPLICATION)
        }

        divisionBtn.setOnClickListener {
            operation = DIVISION
            startGame(DIVISION)
        }

        randomBtn.setOnClickListener {
            operation = RANDOM
            startGame(RANDOM)
        }

        option1Btn.setOnClickListener { onOptionSelected(it) }
        option2Btn.setOnClickListener { onOptionSelected(it) }
        option3Btn.setOnClickListener { onOptionSelected(it) }
        option4Btn.setOnClickListener { onOptionSelected(it) }

        changeStatusColor("#673AB7")


        gotohome.setOnClickListener {
            gameover.visibility = View.GONE  //hides game over view
            dashbord.visibility = View.VISIBLE  //visible dashboard view
        }

        val playagain =  findViewById<Button>(R.id.play_again)

        //restart game
        playagain.setOnClickListener {
            startGame(operation)
            gameover.visibility = View.GONE


        }

    }
    //save highscore both in view model and shared preference
    private fun saveHighScore(score: Int) {
        viewModel.update_high_score(score)   //update high score in the viewmodel
        highScore = score
        val editor = sharedPreferences.edit()
        editor.putInt("highScore", score)  //put new high score into the shared preference
        editor.apply()

        updateHighScore()  //update the ui to to display new high score
    }

    //retrieve high score from the viewmodel
    private fun getHighScore(): Int {
        return viewModel.highScore.value ?: 0
    }

    //change the color of the status bar
    private fun changeStatusColor(color: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = android.graphics.Color.parseColor(color)
        }
    }



    private fun startGame(selectedOperation: Int) {
        option1Btn.setBackgroundResource(R.drawable.rounded_button_background)
        option2Btn.setBackgroundResource(R.drawable.rounded_button_background)
        option3Btn.setBackgroundResource(R.drawable.rounded_button_background)
        option4Btn.setBackgroundResource(R.drawable.rounded_button_background)

        dashbord.visibility = View.GONE
        gamelayout.visibility = View.VISIBLE
        score = 0   //reset score
        currentQstIndex = 0
        showNextQuestion(selectedOperation)
        score = 0
        updateScoreDisplay() //update score display to shw initial score of 0
    }

    private fun showNextQuestion(selectedOperation: Int) {
        currentQuestion = generateRandomQuestion(selectedOperation) //generate new question based on the selected operation
        updateQuestion() //update question ui
        startTimer() //start timer for the question
        questionNumberView.text = "    Question $currentQuestionNumber/50    "  //indicate the current question number out of 50
        currentQuestionNumber++
    }

    private fun updateQuestion() {
        questionView.text = currentQuestion.question  //display current question
        val options = currentQuestion.answers
        option1Btn.text = options[0]   //display answer options
        option2Btn.text = options[1]
        option3Btn.text = options[2]
        option4Btn.text = options[3]
    }

    private fun startTimer() {

        timer?.cancel()
        var timeLeft = 10000L
        progressBar.progress = 100
        val duration = 10000L // Total duration
        val interval = 50L // Update interval

        timer = object : CountDownTimer(duration, interval) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = ((duration - millisUntilFinished) * 100 / duration).toInt()
                progressBar.progress = progress
                timeLeft = millisUntilFinished
                timerTextView.text = "${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                progressBar.progress = 100
                timerTextView.text = "Time's up!"
                endGame()
            }
        }.start()

        soundManager.stop_sound()
        soundManager.play_sound(R.raw.timer)



    }
    //generate random question
    private fun generateRandomQuestion(operation: Int): Question { //generate random numbers
        val num1 = random.nextInt(100)
        val num2 = when (operation) {
            ADDITION, SUBTRACTION, MULTIPLICATION -> random.nextInt(100) + 1 // Adjust the range and operation
            DIVISION -> generateRandomDivisor(num1)
            else -> random.nextInt(100) + 1 // Adjust the range
        }

        val question: String
        val correctAnswer: Int

        when (operation) {
            ADDITION -> {
                question = "$num1 + $num2?"
                correctAnswer = num1 + num2
            }
            SUBTRACTION -> {
                question = "$num1 - $num2?"
                correctAnswer = num1 - num2
            }
            MULTIPLICATION -> {
                question = "$num1 * $num2?"
                correctAnswer = num1 * num2
            }
            DIVISION -> {
                question = "$num1 / $num2?"
                correctAnswer = num1 / num2
            }
            RANDOM -> {
                // choose operation randomly
                val randomOperation = random.nextInt(4)
                return generateRandomQuestion(randomOperation)
            }
            else -> {
                // addition(default)
                question = "$num1 + $num2?"
                correctAnswer = num1 + num2
            }
        }

        val options = generateOptions(correctAnswer)

        return Question(question, options, options.indexOf(correctAnswer.toString()))
    }




    private fun generateRandomDivisor(dividend: Int): Int {
        // Generate a random divisor
        val possibleDivisors = (1..dividend).filter { dividend % it == 0 }
        return possibleDivisors.random()
    }

//generate random answers
    private fun generateOptions(correctAnswer: Int): List<String> {
        val options = mutableListOf<String>()
        options.add(correctAnswer.toString())  //correct answers is added to the list

        while (options.size < 4) {
            val wrongAnswer = correctAnswer + random.nextInt(20) - 10
            if (wrongAnswer != correctAnswer && !options.contains(wrongAnswer.toString())) {  //check wrong answer
                options.add(wrongAnswer.toString())
            }
        }

        return options.shuffled()
    }

    private fun endGame() {

        if (score > getHighScore()) {
            saveHighScore(score) //save new high score
            Toast.makeText(this, "New High Score: $score!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Game Over! Your score: $score", Toast.LENGTH_SHORT).show()
        }
        finalscoretv.setText("Score $score")

        viewModel.update_high_score(score)

        soundManager.stop_sound()
        soundManager.play_sound(R.raw.gameover)

        Handler(Looper.getMainLooper()).postDelayed({
            gameover.visibility = View.VISIBLE
            gamelayout.visibility = View.GONE
            soundManager.stop_sound()
        }, 1000)

        timer?.cancel()
        currentQuestionNumber = 1
    }

    fun onOptionSelected(view: View) {
        val selectedOption = when (view.id) {
            R.id.option1Button -> 0
            R.id.option2Button -> 1
            R.id.option3Button -> 2
            R.id.option4Button -> 3
            else -> -1
        }

        if (selectedOption == currentQuestion.correctAnswer) {
            score += 10  // Increase the score
            updateScoreDisplay()
            view.setBackgroundColor(correctColor)
            soundManager.stop_sound()
            soundManager.play_sound(R.raw.correctanswer)

        } else {
            view.setBackgroundColor(incorrectColor)
            Toast.makeText(this, "Wrong! Game Over.", Toast.LENGTH_SHORT).show()
            endGame()
            return
        }

        Handler(Looper.getMainLooper()).postDelayed({
            view.setBackgroundResource(R.drawable.rounded_button_background)
            currentQstIndex++
            showNextQuestion(operation)  //Pass selected operation
        }, 1000)

    }

    private fun updateHighScore() {  //ui update with latest high score
        val highScoreTextView: TextView = findViewById(R.id.highScoreTextView)
        highScoreTextView.text = "High Score: ${getHighScore()}"
    }

    private fun updateScoreDisplay() {  //update ui to display current score
        viewModel.updateScore(score)
        scoreView.text = "Score: $score"
    }



}