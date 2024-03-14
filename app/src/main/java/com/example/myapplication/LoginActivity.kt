package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.StringRequest
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import android.text.Editable
import android.text.TextWatcher


class LoginActivity : AppCompatActivity() {
    private var usernameEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var loginButton: Button? = null

    // Instantiate the RequestQueue
    private lateinit var queue: RequestQueue
    private val url = "https://nusmb.com/data/login.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Retrieve the qrCodeId from the Intent
        val qrCodeId = intent.getIntExtra("qr_code_id", -1)

        // Find UI elements
        usernameEditText = findViewById(R.id.username_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        loginButton = findViewById<Button>(R.id.login_button)
        val forgotPasswordButton = findViewById<Button>(R.id.forgot_password_button)

        // Add a TextWatcher to usernameEditText to remove spaces
        usernameEditText?.addTextChangedListener(getTextWatcher(usernameEditText))

        // Instantiate the RequestQueue
        queue = Volley.newRequestQueue(this)

        forgotPasswordButton.setOnClickListener {
            val intent = Intent(this, ForgotPassActivity::class.java)
            intent.putExtra("qr_code_id", qrCodeId)
            startActivity(intent)
        }

        // Handle login button click
        loginButton?.setOnClickListener {
            val username = usernameEditText?.text.toString()
            val password = passwordEditText?.text.toString()

            // Send POST request to login.php script
            val stringRequest = object : StringRequest(
                Request.Method.POST, url,
                Response.Listener<String> { response ->
                    Log.d("response", response)
                    if (response == "matched") {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, UnlockActivity::class.java)
                        intent.putExtra("qr_code_id", qrCodeId)
                        startActivity(intent)
                        finish()
                    } else {
                        // If not matched, show an error message
                        Toast.makeText(
                            this,
                            "Invalid username or password. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                object : Response.ErrorListener {
                    override fun onErrorResponse(error: VolleyError) {
                        Log.e("error", error.toString())
                        Toast.makeText(
                            this@LoginActivity,
                            "Error: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["username"] = username
                    params["password"] = password
                    params["ID"] = qrCodeId.toString()
                    return params
                }
            }
            // Add the request to the RequestQueue.
            queue.add(stringRequest)
        }

    }
    private fun getTextWatcher(editText: EditText?): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Remove spaces from the input text
                val filteredText = s?.toString()?.replace(" ", "") ?: ""
                if (s.toString() != filteredText) {
                    editText?.setText(filteredText)
                    editText?.setSelection(filteredText.length)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed
            }
        }
    }
}

