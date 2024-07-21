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
import java.util.*

class LoginTwoFactAuth : AppCompatActivity() {

    private var phoneNumber: String = ""
    private var verificationCode: String = ""
    private val url = "https://nusmb.com/data/unlock.php"
    private val emailUrl = "https://nusmb.com/data/emailcheck.php"
    private val sendToEmailUrl = "https://nusmb.com/data/emailotp.php"

    private lateinit var userEmail: String // To store the retrieved email
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_two_fact_auth)

        // Retrieve the qrCodeId from the Intent
        val qrCodeId = intent.getIntExtra("qr_code_id", -1)

        // Retrieve username and password from Intent
        username = intent.getStringExtra("username") ?: ""

        // Get phone number and generate initial verification code
        getPhoneNumber(qrCodeId)
        generateVerificationCode()

        // Set up onClickListeners for buttons
        val sendCodeButton = findViewById<Button>(R.id.send_code_button)
        sendCodeButton.setOnClickListener {
            // Generate new verification code and send to server
            verificationCode = generateVerificationCode()
            sendVerificationCode(verificationCode, qrCodeId)
            Toast.makeText(this, "Verification code sent to $phoneNumber", Toast.LENGTH_SHORT).show()
            Log.d("Verification Code", verificationCode)
        }

        val sendToEmailButton = findViewById<Button>(R.id.send_to_email_button)
        sendToEmailButton.setOnClickListener {
            Toast.makeText(this, "Sending...", Toast.LENGTH_SHORT).show()

            // Check if userEmail is already retrieved
            if (::userEmail.isInitialized && userEmail.isNotEmpty()) {
                // Generate a new verification code before sending to email
                verificationCode = generateVerificationCode()
                sendVerificationToEmail(userEmail, username, verificationCode)
            } else {
                // Retrieve email first, then generate and send verification code
                getEmailAndSendVerification(username)
            }
        }

        val verifyButton = findViewById<Button>(R.id.verify_button)
        verifyButton.setOnClickListener {
            // Check if entered verification code matches generated code
            val enteredCode = findViewById<EditText>(R.id.verification_text).text.toString()
            if (enteredCode.isNotBlank() && enteredCode == verificationCode) {
                // Verification successful
                sendRequest(0, qrCodeId)
                Toast.makeText(this, "Verification successful!", Toast.LENGTH_SHORT).show()

                // Call updateno1.php
                val updateUrl = "https://nusmb.com/data/updateno1.php"
                val updateRequest = object : StringRequest(
                    Request.Method.POST, updateUrl,
                    Response.Listener<String> { response ->
                        // Handle successful response
                        Log.d("UpdateResponse", response)
                    },
                    Response.ErrorListener { error ->
                        // Handle error response
                        showToast("Error updating No column: ${error.message}")
                    }
                ) {
                    override fun getParams(): MutableMap<String, String> {
                        val params = HashMap<String, String>()
                        params["No"] = qrCodeId.toString()
                        return params
                    }
                }
                Volley.newRequestQueue(this).add(updateRequest)

                // Call erase.php
                val eraseUrl = "https://nusmb.com/data/erase.php"
                val eraseRequest = object : StringRequest(
                    Request.Method.POST, eraseUrl,
                    Response.Listener<String> { response ->
                        // Handle successful response
                        Log.d("EraseResponse", response)
                    },
                    Response.ErrorListener { error ->
                        // Handle error response
                        showToast("Error erasing data: ${error.message}")
                    }
                ) {
                    override fun getParams(): MutableMap<String, String> {
                        val params = HashMap<String, String>()
                        params["ID"] = qrCodeId.toString()
                        return params
                    }
                }
                Volley.newRequestQueue(this).add(eraseRequest)

                // Proceed to ThankyouActivity
                startActivity(Intent(this, ThankyouActivity::class.java))
                finish()
            } else {
                // Verification failed
                Toast.makeText(this, "Verification failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendVerificationToEmail(email: String, username: String, newVerificationCode: String) {
        val queue = Volley.newRequestQueue(this)
        val url = sendToEmailUrl

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                // Handle successful response
                Toast.makeText(this, "Verification code sent to $email", Toast.LENGTH_SHORT).show()
                Log.d("Verification Code", newVerificationCode)
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

    private fun sendRequest(parking: Int, qrCodeId: Int) {
        val queue = Volley.newRequestQueue(this)
        val request = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                // Handle successful response
                if (response == "Parking column updated successfully") {
                    if (parking == 0) {
                        showToast("Unlocked")
                    }
                }
            },
            Response.ErrorListener { error ->
                // Handle error response
                showToast("Error: ${error.message}")
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["Parking"] = parking.toString()
                params["ID"] = qrCodeId.toString()
                return params
            }
        }
        queue.add(request)
    }

    private fun getEmailAndSendVerification(username: String) {
        val urlWithParams = "$emailUrl?User=$username"

        val stringRequest = StringRequest(Request.Method.GET, urlWithParams,
            { response ->
                userEmail = response.trim() // Assuming response is the email address
                Log.d("UserEmail", userEmail)
                // Generate new verification code and send to email
                verificationCode = generateVerificationCode()
                sendVerificationToEmail(userEmail, username, verificationCode)
            },
            { error ->
                // Handle error
                showToast("Error getting email: ${error.message}")
            })

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getPhoneNumber(qrCodeId: Int) {
        val url = "https://nusmb.com/data/phonenum.php?ID=${qrCodeId}" // Append the parameter to the URL

        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                if (response == "No Phone Number") {
                    // If no phone number, do nothing
                } else {
                    // Save the response into Phone Number
                    phoneNumber = response.trim()
                }
            },
            { error ->
                // Handle error
            })

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun generateVerificationCode(): String {
        val random = Random()
        return String.format("%06d", random.nextInt(1000000))
    }

    private fun sendVerificationCode(verificationCode: String, qrCodeId: Int) {
        val url = "https://nusmb.com/data/logintwofactauth.php" // Replace with your URL

        val stringRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener { response ->
                // Handle response
            },
            Response.ErrorListener { error ->
                // Handle error
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["verification_code"] = verificationCode
                params["ID"] = qrCodeId.toString()
                return params
            }
        }

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(stringRequest)
    }
}
