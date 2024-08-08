package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class UnlockActivity : AppCompatActivity() {

    private val checkUrl = "https://nusmb.com/data/parkingcheck.php"
    private val unlockUrl = "https://nusmb.com/data/unlock.php"
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unlock)

        // Retrieve the qrCodeId from the Intent
        val qrCodeId = intent.getIntExtra("qr_code_id", -2)

        // Set the title dynamically
        val titleTextView = findViewById<TextView>(R.id.title_textview)
        titleTextView.text = "Smart Bike Parking No. $qrCodeId"

        val unlockButton = findViewById<ImageButton>(R.id.unlock_button)
        val lockButton = findViewById<ImageButton>(R.id.lock_button)
        val imageButton = findViewById<ImageButton>(R.id.image_button)

        val username = intent.getStringExtra("username") ?: ""
        val password = intent.getStringExtra("password") ?: ""

        // Initial check and update every second
        updateParkingStatus(qrCodeId, unlockButton, lockButton)

        unlockButton.setOnClickListener {
            val intent = Intent(this, LoginTwoFactAuth::class.java)
            intent.putExtra("qr_code_id", qrCodeId)
            intent.putExtra("username", username)
            intent.putExtra("password", password)
            startActivity(intent)
        }

        lockButton.setOnClickListener {
            sendRequest(1, qrCodeId)
            startActivity(Intent(this, LPromptActivity::class.java))
        }

        imageButton.setOnClickListener {
            val intent = Intent(this, ImagesActivity::class.java)
            intent.putExtra("qr_code_id", qrCodeId)
            startActivity(intent)
        }
    }

    private fun updateParkingStatus(qrCodeId: Int, unlockButton: ImageButton, lockButton: ImageButton) {
        val checkUrlWithParams = "$checkUrl?ID=$qrCodeId"

        val queue = Volley.newRequestQueue(this)
        val request = StringRequest(
            Request.Method.GET, checkUrlWithParams,
            Response.Listener<String> { response ->
                // Handle successful response
                if (response.trim() == "1") {
                    // Make lock button unclickable
                    lockButton.isEnabled = false
                    lockButton.setImageResource(R.drawable.buttonlockgray)
                    unlockButton.isEnabled = true
                    unlockButton.setImageResource(R.drawable.buttonulock)
                } else if (response.trim() == "0") {
                    // Make unlock button unclickable
                    unlockButton.isEnabled = false
                    unlockButton.setImageResource(R.drawable.buttonulockgray)
                    lockButton.isEnabled = true
                    lockButton.setImageResource(R.drawable.buttonlock)
                }
                // Schedule the next update after 1 second
                handler.postDelayed({ updateParkingStatus(qrCodeId, unlockButton, lockButton) }, 1000)
            },
            Response.ErrorListener { error ->
                // Handle error response
                showToast("Error: ${error.message}")
                // Schedule the next update after 1 second even if there's an error
                handler.postDelayed({ updateParkingStatus(qrCodeId, unlockButton, lockButton) }, 1000)
            }
        )
        queue.add(request)
    }

    private fun sendRequest(parking: Int, qrCodeId: Int) {
        val queue = Volley.newRequestQueue(this)
        val request = object : StringRequest(
            Request.Method.POST, unlockUrl,
            Response.Listener<String> { response ->
                // Handle successful response
                if (response == "Parking column updated successfully") {
                    if (parking == 1) {
                        showToast("Locked")
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove any remaining callbacks to prevent memory leaks
        handler.removeCallbacksAndMessages(null)
    }
}
