package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class LPromptActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lprompt)

        val doneButton: Button = findViewById(R.id.btn_done)
        doneButton.setOnClickListener {
            onBackPressed()
        }
    }
}

