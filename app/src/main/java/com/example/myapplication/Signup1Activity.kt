package com.example.myapplication

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.content.Intent
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.StringRequest
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.CheckBox

class Signup1Activity : AppCompatActivity() {

    private val resetUrl = "https://nusmb.com/data/reset.php"
    private lateinit var queue: RequestQueue

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup1)

        // Retrieve the qrCodeId from the Intent
        val qrCodeId = intent.getIntExtra("UserId", -1)
        val phoneNumber = intent.getStringExtra("PhoneNum")

        // Find UI elements
        val usernameEditText = findViewById<EditText>(R.id.username_edit_text)
        val passwordEditText = findViewById<EditText>(R.id.password_edit_text)
        val confirmButton = findViewById<Button>(R.id.confirm_button)
        val showPasswordCheckBox = findViewById<CheckBox>(R.id.show_password_checkbox)

        // Initialize Volley queue
        queue = Volley.newRequestQueue(this)

        // Add a TextWatcher to usernameEditText to remove spaces
        usernameEditText.addTextChangedListener(getTextWatcher(usernameEditText))

        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Show password
                passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                // Hide password
                passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

        confirmButton.setOnClickListener {
            val username = usernameEditText.text.toString().replace(" ", "") // Remove spaces before submitting
            val password = passwordEditText.text.toString().replace(" ", "") // Remove spaces before submitting

            // Create a String request
            val stringRequest = object : StringRequest(
                Request.Method.POST, resetUrl,
                Response.Listener<String> { response ->
                    // Handle response
                    Toast.makeText(this, "Reset successful. You may now use your account.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SignupActivity::class.java)
                    startActivity(intent)
                    finish()
                },
                Response.ErrorListener { error ->
                    // Handle error
                    Toast.makeText(this, "Error occurred: ${error.message}", Toast.LENGTH_SHORT).show()
                }) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["ID"] = qrCodeId.toString()
                    params["User"] = username
                    params["Pass"] = password
                    return params
                }
            }

            // Add the request to the RequestQueue.
            queue.add(stringRequest)
        }
    }

    private fun getTextWatcher(editText: EditText): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Remove spaces from the input text
                val filteredText = s?.toString()?.replace(" ", "") ?: ""
                if (s.toString() != filteredText) {
                    editText.setText(filteredText)
                    editText.setSelection(filteredText.length)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed
            }
        }
    }
}
