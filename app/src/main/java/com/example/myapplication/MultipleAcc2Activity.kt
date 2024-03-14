package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import android.util.Log
import android.widget.Button
import android.widget.EditText
import java.util.HashMap

class MultipleAcc2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiple_acc2)

        // Retrieve the UserId from the intent
        val userId = intent.getIntExtra("UserId",-1)
        val phonenum = intent.getStringExtra("PhoneNum")
        Log.d("UserId", "Retrieved UserId: $userId")

        val verificationCode = generateVerificationCode()

        sendVerificationCode(verificationCode, userId)

        val verifyButton = findViewById<Button>(R.id.verify_button)
        verifyButton.setOnClickListener{
            val enteredCode = findViewById<EditText>(R.id.verification_code_edit_text).text.toString()
            if (enteredCode.isNotBlank() && enteredCode == verificationCode){
                nullifyUserAndPass(userId)
                val intent = Intent(this, Signup1Activity::class.java)
                intent.putExtra("UserId", userId)
                intent.putExtra("PhoneNum", phonenum)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun generateVerificationCode(): String {
        val random = java.util.Random()
        val code = StringBuilder()
        repeat(6) {
            val digit = random.nextInt(10)
            code.append(digit)
        }
        return code.toString()
    }

    private fun sendVerificationCode(verificationCode: String, qrCodeId: Int) {
        val url = "https://nusmb.com/data/logintwofactauth1.php" // Replace with your URL

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
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun nullifyUserAndPass(userId: Int) {
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
                val params = HashMap<String, String>()
                params["ID"] = userId.toString()
                return params
            }
        }
        Volley.newRequestQueue(this).add(nullifyRequest)
    }
}
