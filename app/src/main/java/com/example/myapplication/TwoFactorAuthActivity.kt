package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.StringRequest

class TwoFactorAuthActivity : AppCompatActivity() {

    private lateinit var phoneNumberEditText: EditText
    private lateinit var sendCodeButton: Button
    private lateinit var verificationCodeEditText: EditText
    private lateinit var verifyButton: Button
    private lateinit var phoneurl: String

    private var verificationCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_two_factor_auth)

        val username = intent.getStringExtra("username") ?: ""
        val password = intent.getStringExtra("password") ?: ""

        // Find UI elements
        phoneNumberEditText = findViewById(R.id.phone_number_edit_text)
        sendCodeButton = findViewById(R.id.send_code_button)
        verificationCodeEditText = findViewById(R.id.verification_code_edit_text)
        verifyButton = findViewById(R.id.verify_button)


// Handle send code button click
        sendCodeButton.setOnClickListener {
            val phoneNumber = phoneNumberEditText.text.toString()

            // Generate random 6-digit code
            verificationCode = generateVerificationCode()

            val queue = Volley.newRequestQueue(this)
            val url = "https://nusmb.com/data/signupaccount1.php"

            val stringRequest = object : StringRequest(
                Request.Method.POST, url,
                Response.Listener<String> { response ->
                    // Handle successful response
                    Toast.makeText(this, "Verification code sent to $phoneNumber", Toast.LENGTH_SHORT).show()

                    // Print verification code to log
                    Log.d("Verification Code", verificationCode!!)

                    // Save verification code to SharedPreferences
                    saveVerificationCode()
                },
                Response.ErrorListener { error ->
                    // Handle error
                    Log.e("TwoFactorAuthActivity", "Error sending verification code", error)
                    Toast.makeText(this, "Error sending verification code", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["TwoFactAuth"] = verificationCode!!
                    params["User"] = username
                    params["Pass"] = password
                    params["PhoneNum"] = phoneNumber
                    return params
                }
            }

            queue.add(stringRequest)
        }

        verifyButton.setOnClickListener {
            val enteredCode = verificationCodeEditText.text.toString()
            val queue = Volley.newRequestQueue(this)

            if (enteredCode == verificationCode) {
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

                queue.add(signUpRequest) // Add the request to the queue

                // Start the SignupActivity
                val intent = Intent(this, SignupActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Verification failed, display error message
                Toast.makeText(this, "Verification failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun generateVerificationCode(): String {
        val random = Random()
        val code = random.nextInt(1000000)
        return String.format("%06d", code)
    }

    // Save verification code to SharedPreferences
    private fun saveVerificationCode() {
        val sharedPref = getSharedPreferences("verification_code", Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putString("code", verificationCode)
        }.apply()
    }
}