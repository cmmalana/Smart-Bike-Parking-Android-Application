package com.example.myapplication

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.*

class PersonalInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info)

        val firstName: EditText = findViewById(R.id.firstName)
        val middleName: EditText = findViewById(R.id.middleName)
        val lastName: EditText = findViewById(R.id.lastName)
        val birthday: EditText = findViewById(R.id.birthday)
        val phoneNumber: EditText = findViewById(R.id.phoneNumber)
        val emailAddress: EditText = findViewById(R.id.emailAddress)
        val submitButton: Button = findViewById(R.id.submitButton)

        // Retrieve the intent and extract the username and password
        val username = intent.getStringExtra("username") ?: ""
        val password = intent.getStringExtra("password") ?: ""

        // Initially disable the submit button
        submitButton.isEnabled = false

        // Set up DatePickerDialog for the birthday EditText
        val datePickerListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(
                    this,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        val formattedDate = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
                        birthday.setText(formattedDate)
                        phoneNumber.requestFocus()
                    },
                    year, month, day
                )
                datePickerDialog.show()
            }
        }

        birthday.onFocusChangeListener = datePickerListener

        // TextWatcher to check if all required fields are filled
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                submitButton.isEnabled = firstName.text.isNotEmpty() &&
                        lastName.text.isNotEmpty() &&
                        birthday.text.isNotEmpty() &&
                        phoneNumber.text.isNotEmpty() &&
                        emailAddress.text.isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        // Add TextWatcher to required fields
        firstName.addTextChangedListener(textWatcher)
        lastName.addTextChangedListener(textWatcher)
        birthday.addTextChangedListener(textWatcher)
        phoneNumber.addTextChangedListener(textWatcher)
        emailAddress.addTextChangedListener(textWatcher)

        // Move to the next EditText on Enter key press
        firstName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                middleName.requestFocus()
                true
            } else {
                false
            }
        }
        middleName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                lastName.requestFocus()
                true
            } else {
                false
            }
        }
        lastName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                birthday.requestFocus()
                true
            } else {
                false
            }
        }
        phoneNumber.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                emailAddress.requestFocus()
                true
            } else {
                false
            }
        }
        emailAddress.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Perform validation if necessary
                submitButton.performClick()
                true
            } else {
                false
            }
        }

        submitButton.setOnClickListener {
            val firstNameText = firstName.text.toString()
            val middleNameText = middleName.text.toString()
            val lastNameText = lastName.text.toString()
            val birthdayText = birthday.text.toString()
            val phoneNumberText = phoneNumber.text.toString()
            val emailAddressText = emailAddress.text.toString()

            // Generate a 6-digit OTP
            val otp = (100000..999999).random()

            // Save the OTP as TwoFactAuth (you might want to save this in SharedPreferences, database, etc.)
            val twoFactAuth = otp.toString()

            // Create a request queue
            val requestQueue: RequestQueue = Volley.newRequestQueue(this)

            // Create a StringRequest
            val stringRequest = object : StringRequest(
                Request.Method.POST, "https://nusmb.com/data/personalinfo.php",
                Response.Listener { response ->
                    try {
                        val jsonResponse = JSONObject(response)
                        val status = jsonResponse.getString("status")
                        val message = jsonResponse.getString("message")

                        if (status == "success") {
                            Toast.makeText(this, "$message", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, TwoFactorAuthActivity::class.java).apply {
                                putExtra("PhoneNum", phoneNumberText)
                                putExtra("Email", emailAddressText)
                                putExtra("TwoFactAuth", twoFactAuth)
                                putExtra("username", username)
                                putExtra("password", password)
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_LONG).show()
                    }
                },
                Response.ErrorListener { error ->
                    // Handle error
                    Toast.makeText(this, "Error. Please Try Again. ${error.message}", Toast.LENGTH_LONG).show()
                }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["User"] = username
                    params["Pass"] = password
                    params["TwoFactAuth"] = twoFactAuth
                    params["PhoneNum"] = phoneNumberText
                    params["Email"] = emailAddressText
                    params["FName"] = firstNameText
                    params["MName"] = middleNameText
                    params["LName"] = lastNameText
                    params["Bday"] = birthdayText
                    return params
                }
            }

            // Add the request to the request queue
            requestQueue.add(stringRequest)
        }
    }
}
