package com.example.quizgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main1)

        var level2 = findViewById<Button>(R.id.level2)
        val playerNameEditText = findViewById<EditText>(R.id.level1)
        level2.setOnClickListener() {
            val playerName = playerNameEditText.text.toString()
            if (playerName.isNotEmpty()) {
                Toast.makeText(this, "Hello $playerName!", Toast.LENGTH_SHORT).show()
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }




    }
}
