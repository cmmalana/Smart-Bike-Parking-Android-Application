package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class ThankyouActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thankyou)

        // Display thank you message
        val thankYouMessage = "Thank you for using Smart Bike Parking. Don't forget to remove your bicycle and take your helmet."
        val messageTextView: TextView = findViewById(R.id.thankyou_message)
        messageTextView.text = thankYouMessage

        val doneButton: Button = findViewById(R.id.btn_done)
        doneButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
