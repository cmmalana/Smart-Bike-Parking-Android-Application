package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.*

class TwoFactorAuthActivity : AppCompatActivity() {

    private lateinit var sendCodeButton: Button
    private lateinit var sendToEmailButton: Button
    private lateinit var verificationCodeEditText: EditText
    private lateinit var verifyButton: Button

    private var verificationCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_two_factor_auth)

        val username = intent.getStringExtra("username") ?: ""
        val password = intent.getStringExtra("password") ?: ""
        val phoneNum = intent.getStringExtra("PhoneNum") ?: ""
        val email = intent.getStringExtra("Email") ?: ""
        verificationCode = generateVerificationCode()

        // Find UI elements
        sendCodeButton = findViewById(R.id.send_code_button)
        sendToEmailButton = findViewById(R.id.send_to_email_button)
        verificationCodeEditText = findViewById(R.id.verification_code_edit_text)
        verifyButton = findViewById(R.id.verify_button)

        // Handle send code button click (for phone number)
        sendCodeButton.setOnClickListener {
            verificationCode = generateVerificationCode()
            sendVerificationCode(phoneNum, username, password, verificationCode!!)
        }

        // Handle send to email button click
        sendToEmailButton.setOnClickListener {
            verificationCode = generateVerificationCode()
            Toast.makeText(this, "Sending...", Toast.LENGTH_SHORT).show()
            sendVerificationToEmail(email, username, password, verificationCode!!)
        }

        // Handle verify button click
        verifyButton.isEnabled = false // Initially disable verify button
        verifyButton.setOnClickListener {
            val enteredCode = verificationCodeEditText.text.toString()

            if (enteredCode.length == 6 && enteredCode == verificationCode) {
                handleVerificationSuccess(username, password)
            } else {
                Toast.makeText(this, "Verification failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }

        // Add text change listener to enable/disable verify button
        verificationCodeEditText.addTextChangedListener {
            val code = it.toString()
            verifyButton.isEnabled = code.length == 6
        }
    }

    private fun sendVerificationCode(phoneNum: String, username: String, password: String, newVerificationCode: String) {
        startCountdown(sendCodeButton)

        val queue = Volley.newRequestQueue(this)
        val url = "https://nusmb.com/data/signupaccount1.php"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                // Handle successful response
                Toast.makeText(this, "Verification code sent to $phoneNum", Toast.LENGTH_SHORT).show()

                // Print verification code to log
                Log.d("Verification Code", newVerificationCode)

                // Save verification code to SharedPreferences
                saveVerificationCode(newVerificationCode)
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e("TwoFactorAuthActivity", "Error sending verification code", error)
                Toast.makeText(this, "Error sending verification code", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["TwoFactAuth"] = newVerificationCode
                params["User"] = username
                params["Pass"] = password
                params["PhoneNum"] = phoneNum
                return params
            }
        }

        queue.add(stringRequest)
    }

    private fun sendVerificationToEmail(email: String, username: String, password: String, newVerificationCode: String) {
        startCountdown(sendToEmailButton)

        val queue = Volley.newRequestQueue(this)
        val url = "https://nusmb.com/data/emailotp.php"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                // Handle successful response
                Toast.makeText(this, "Verification code sent to $email", Toast.LENGTH_SHORT).show()

                // Print verification code to log
                Log.d("Verification Code", newVerificationCode)

                // Save verification code to SharedPreferences
                saveVerificationCode(newVerificationCode)
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e("TwoFactorAuthActivity", "Error sending verification code to email", error)
                Toast.makeText(this, "Error sending verification code to email", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["TwoFactAuth"] = newVerificationCode
                params["User"] = username
                params["Email"] = email
                return params
            }
        }

        queue.add(stringRequest)
    }

    private fun handleVerificationSuccess(username: String, password: String) {
        Toast.makeText(this, "Verification successful! Please Login", Toast.LENGTH_SHORT).show()

        // Create a POST request to update SignUp status
        val signUpUrl = "https://nusmb.com/data/updatesignup.php" // Change this URL to your actual PHP endpoint

        val signUpRequest = object : StringRequest(
            Request.Method.POST, signUpUrl,
            Response.Listener<String> { response ->
                // Handle the response if needed
                Log.d("SignUpResponse", response)
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e("SignUpError", error.toString())
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user"] = username
                params["pass"] = password
                return params
            }
        }

        Volley.newRequestQueue(this).add(signUpRequest) // Add the request to the queue

        // Start the SignupActivity
        startActivity(Intent(this, SignupActivity::class.java))
        finish()
    }

    private fun generateVerificationCode(): String {
        val random = Random()
        return String.format("%06d", random.nextInt(1000000))
    }

    private fun startCountdown(button: Button) {
        button.isEnabled = false // Disable the button
        button.isClickable = false // Ensure the button is not clickable

        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                button.text = "Resend Code (${secondsLeft}s)"
                button.alpha = 0.5f // Reduce button opacity to indicate disabled state
            }

            override fun onFinish() {
                button.isEnabled = true // Enable the button
                button.isClickable = true // Make the button clickable
                button.text = when (button.id) {
                    R.id.send_code_button -> "SEND CODE TO PHONE NUMBER"
                    R.id.send_to_email_button -> "SEND CODE TO EMAIL"
                    else -> "Button"
                }
                button.alpha = 1.0f // Restore full opacity
            }
        }.start()
    }

    // Save verification code to SharedPreferences
    private fun saveVerificationCode(code: String) {
        val sharedPref = getSharedPreferences("verification_code", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("code", code)
            apply()
        }
    }
}
