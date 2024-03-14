package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var messageTextView: TextView
    private lateinit var agreeButton: Button
    private lateinit var disagreeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageTextView = findViewById(R.id.messageTextView)
        agreeButton = findViewById(R.id.agreeButton)
        disagreeButton = findViewById(R.id.disagreeButton)

        agreeButton.setOnClickListener {
            val intent = Intent(this, InstructionsActivity::class.java)
            startActivity(intent)
        }

        disagreeButton.setOnClickListener {
            finishAffinity()
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}
