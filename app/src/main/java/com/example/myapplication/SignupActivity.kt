package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import android.text.method.PasswordTransformationMethod
import android.widget.CheckBox
import android.text.method.HideReturnsTransformationMethod
import android.view.inputmethod.EditorInfo

class SignupActivity : AppCompatActivity() {

    private val url = "https://nusmb.com/data/signupaccount.php"
    private val loginUrl = "https://nusmb.com/data/loginaccount.php"
    private lateinit var queue: RequestQueue

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var newusernameEditText: EditText
    private lateinit var newpasswordEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Find UI elements
        usernameEditText = findViewById(R.id.username_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        newusernameEditText = findViewById(R.id.new_username_edit_text)
        newpasswordEditText = findViewById(R.id.new_password_edit_text)
        signupButton = findViewById(R.id.signup_button)
        loginButton = findViewById(R.id.login_button)
        val showPasswordCheckBox = findViewById<CheckBox>(R.id.show_password_checkbox)
        val showNewPasswordCheckBox = findViewById<CheckBox>(R.id.show_new_password_checkbox)
        val forgotPasswordButton = findViewById<Button>(R.id.forgot_password_button)

        // Initially disable the Login and Signup button
        loginButton.isEnabled = false
        signupButton.isEnabled = false

        // TextWatcher for username and password fields
        val usernamePasswordWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val username = usernameEditText.text.toString()
                val password = passwordEditText.text.toString()
                val newUsername = newusernameEditText.text.toString()
                val newPassword = newpasswordEditText.text.toString()

                loginButton.isEnabled = username.isNotEmpty() && password.isNotEmpty()
                signupButton.isEnabled = newUsername.isNotEmpty() && newPassword.isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        usernameEditText.addTextChangedListener(usernamePasswordWatcher)
        passwordEditText.addTextChangedListener(usernamePasswordWatcher)
        newusernameEditText.addTextChangedListener(usernamePasswordWatcher)
        newpasswordEditText.addTextChangedListener(usernamePasswordWatcher)

        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Show password
                passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                // Hide password
                passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

        showNewPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Show password
                newpasswordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                // Hide password
                newpasswordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

        // Add a TextWatcher to usernameEditText to remove spaces
        usernameEditText.addTextChangedListener(getTextWatcher(usernameEditText))

        // Add a TextWatcher to newusernameEditText to remove spaces
        newusernameEditText.addTextChangedListener(getTextWatcher(newusernameEditText))

        // Next Section when clicking the Enter button in keyboard
        usernameEditText.imeOptions = EditorInfo.IME_ACTION_NEXT
        usernameEditText.inputType = InputType.TYPE_CLASS_TEXT
        newusernameEditText.imeOptions = EditorInfo.IME_ACTION_NEXT
        newusernameEditText.inputType = InputType.TYPE_CLASS_TEXT

        queue = Volley.newRequestQueue(this)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            val stringRequest = object : StringRequest(
                Method.POST, loginUrl,
                Response.Listener { response ->
                    Log.d("response", response)
                    when (response) {
                        "No matching user found." -> {
                            // If not matched, show an error message
                            Toast.makeText(
                                this,
                                "Invalid username or password.",
                                Toast.LENGTH_LONG
                            ).show()
                            Toast.makeText(this, "Please try again.", Toast.LENGTH_SHORT).show()
                        }
                        "0" -> {
                            Toast.makeText(this, "Login successful! Please Scan QR Code", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, QRScannerActivity::class.java)
                            intent.putExtra("username", username)
                            intent.putExtra("password", password)
                            startActivity(intent)
                            finish()
                        }
                        else -> {
                            val noValue = response.toInt()
                            Toast.makeText(this, "Login successful! Please go to No. $response", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, UnlockActivity::class.java)
                            intent.putExtra("qr_code_id", noValue)
                            intent.putExtra("username", username)
                            intent.putExtra("password", password)
                            startActivity(intent)
                            finish()
                        }
                    }
                },
                Response.ErrorListener { error ->
                    Log.e("error", error.toString())
                    Toast.makeText(
                        this@SignupActivity,
                        "Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["user"] = username
                    params["pass"] = password
                    return params
                }
            }

            queue.add(stringRequest)
        }


        signupButton.setOnClickListener {
            val username = newusernameEditText.text.toString()
            val password = newpasswordEditText.text.toString()

            val request = object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    when (response) {
                        "0" -> {
                            //Toast.makeText(this, "Sign Up success!", Toast.LENGTH_LONG).show()
                            //val intent = Intent(this, TwoFactorAuthActivity::class.java)
                            val intent = Intent(this, PersonalInfoActivity::class.java)
                            intent.putExtra("username", username)
                            intent.putExtra("password", password)
                            startActivity(intent)
                            finish()
                        }
                        "1" -> {
                            Toast.makeText(this, "Username and Password is already registered.", Toast.LENGTH_LONG).show()
                            Toast.makeText(this, "Please Login Instead.", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Toast.makeText(this, "Error: Sign up failed. Try again.", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                Response.ErrorListener { error ->
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                }) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["user"] = username
                    params["pass"] = password
                    return params
                }
            }
            queue.add(request)
        }

        // Navigate to ForgotPassActivity when Forgot Password button is clicked
        forgotPasswordButton.setOnClickListener {
            val intent = Intent(this, ForgotPassActivity::class.java)
            startActivity(intent)
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
