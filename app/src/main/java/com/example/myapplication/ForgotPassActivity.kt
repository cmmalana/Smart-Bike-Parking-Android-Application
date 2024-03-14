package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlin.random.Random

class ForgotPassActivity : AppCompatActivity() {

    private lateinit var phoneNumberEditText: EditText
    private lateinit var sendCodeButton: Button
    private lateinit var verificationEditText: EditText
    private lateinit var verifyButton: Button

    private var phoneNumber: String = ""
    private var verificationCode: String = ""
    private var id: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass)

        phoneNumberEditText = findViewById(R.id.phone_number_text)
        sendCodeButton = findViewById(R.id.send_code_button)
        verificationEditText = findViewById(R.id.verification_text)
        verifyButton = findViewById(R.id.verify_button)

        sendCodeButton.setOnClickListener {
            // Get the phone number from the EditText
            phoneNumber = phoneNumberEditText.text.toString()

            // Send the phone number using POST request
            sendPhoneNumber()

            //POST a text message
            sendIDAndVerificationCode(verificationCode, id)
        }

        // Handle verify button click
        verifyButton.setOnClickListener {
            // Implement your logic to verify code
            val enteredVerificationCode = verificationEditText.text.toString()
            if (enteredVerificationCode == verificationCode && enteredVerificationCode.isNotBlank()) {
                // Verification code matched, navigate to Signup1Activity
                Toast.makeText(this, "Verification success!", Toast.LENGTH_SHORT).show()
                nullifyUserAndPass(id)
                val intent = Intent(this, Signup1Activity::class.java)
                intent.putExtra("PhoneNum", phoneNumber)
                intent.putExtra("UserId", id.toInt())
                startActivity(intent)
                finish()
            } else {
                // Verification code does not match, show error message
                Toast.makeText(this, "Invalid verification code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendPhoneNumber() {
        // Get the phone number from the EditText
        val phoneNumber = phoneNumberEditText.text.toString().trim()

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val url = "https://nusmb.com/data/forgotpass.php"

        // Request a string response from the provided URL.
        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response from the server
                Log.d("Response", response)
                if (response.trim() == "0") {
                    // Phone number not found
                    Toast.makeText(this, "$phoneNumber not found. Please Try Again", Toast.LENGTH_SHORT).show()
                } else if (response.trim() == "2"){
                    val intent = Intent(this, MultipleAccActivity::class.java)
                    intent.putExtra("PhoneNum", phoneNumber)
                    startActivity(intent)
                    finish()
                } else {
                    // Phone number found, parse the response to get phone number and ID
                    val parts = response.split(", ")
                    if (parts.size == 2) {
                        val phoneNumber = parts[0].substringAfter("Phone Number: ")
                        id = parts[1].substringAfter("ID: ")
                        // Save phone number and ID
                        this.phoneNumber = phoneNumber
                        // Output phone number value and its ID
                        Log.d("Phone Number", "Phone Number: $phoneNumber, ID: $id")

                        Toast.makeText(this, "Verification code sent to $phoneNumber", Toast.LENGTH_SHORT).show()

                        // Generate verification code
                        generateVerificationCode()

                        // Send ID and verification code to logintwofactauth.php
                        sendIDAndVerificationCode(verificationCode, id)
                    }
                }
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e("Error", error.toString())
                // Here you can handle the error, e.g., show a toast
                Toast.makeText(this, "Error sending phone number", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["PhoneNum"] = phoneNumber
                return params
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    private fun generateVerificationCode() {
        // Generate a 6-digit verification code
        val min = 100000
        val max = 999999
        verificationCode = (Random.nextInt(min, max + 1)).toString()
        Log.d("VerificationCode", "Generated verification code: $verificationCode")
    }

    private fun sendIDAndVerificationCode(verificationCode: String, id: String) {

        val url = "https://nusmb.com/data/logintwofactauth1.php" // Replace with your URL

        val stringRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener { response ->
                // Handle response
            },
            Response.ErrorListener { error ->
                // Handle error
            }) {
            override fun getParams(): Map<String, String> {
                val params = java.util.HashMap<String, String>()
                params["verification_code"] = this@ForgotPassActivity.verificationCode
                params["ID"] = id
                return params
            }
        }
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun nullifyUserAndPass(userId: String) {
        val nullifyUrl = "https://nusmb.com/data/erase1.php"

        val nullifyRequest = object : StringRequest(Request.Method.POST, nullifyUrl,
            Response.Listener { response ->
                // Handle response
                Log.d("Nullify Response", response)
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e("Nullify Error", "Error occurred while nullifying user and pass: $error")
            }) {
            override fun getParams(): Map<String, String> {
                val params = java.util.HashMap<String, String>()
                params["ID"] = userId
                return params
            }
        }
        Volley.newRequestQueue(this).add(nullifyRequest)
    }
}