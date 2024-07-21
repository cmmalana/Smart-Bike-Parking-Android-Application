package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback

class QRScannerActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner
    private var username: String? = null
    private var password: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrscanner)

        // Retrieve username and password from Intent extras
        username = intent.getStringExtra("username")
        password = intent.getStringExtra("password")

        // Initialize CodeScanner
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        scannerView.setOnClickListener {
            scannerView.requestFocus()
        }
        codeScanner = CodeScanner(this, scannerView)

        // Set callback for scanned QR code
        codeScanner.decodeCallback = object : DecodeCallback {
            override fun onDecoded(result: com.google.zxing.Result) {
                runOnUiThread {
                    val scannedQRCode = result.text
                    Log.d("QRScannerActivity", "Scanned QR code: $scannedQRCode")
                    checkQRCode(scannedQRCode)
                }
            }
        }

        // Start scanning
        codeScanner.startPreview()
    }

    private fun checkQRCode(qrCodeValue: String) {
        val url = "https://nusmb.com/data/qr.php"
        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener { response ->
                // Handle response from the server
                if (response.trim() == "0") {
                    Toast.makeText(this, "Invalid QR Code. Please try again.", Toast.LENGTH_SHORT).show()
                    // Restart the activity if needed
                    // val intent = Intent(this, QRScannerActivity::class.java)
                    // startActivity(intent)
                    // finish()
                } else {
                    // QR code exists in the database
                    val noValue = response.toInt()
                    checkUserExistence(noValue, qrCodeValue)
                }
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e("Volley Error", error.toString())
                Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show()
            }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["qrcode_value"] = qrCodeValue
                return params
            }
        }

        requestQueue.add(stringRequest)
    }

    private fun checkUserExistence(noValue: Int, qrCodeValue: String) {
        val url = "https://nusmb.com/data/qr1.php"
        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener { response ->
                // Handle response from the server
                if (response.trim() == "0") {
                    // User doesn't exist, proceed with updating user info
                    Toast.makeText(this, "Welcome to Smart Bike Parking!", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "You may now access No. $noValue", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "Account is now connected to No. $noValue", Toast.LENGTH_SHORT).show()
                    updateNoAndUserInfo(qrCodeValue, noValue.toString())
                    val intent = Intent(this, UnlockActivity::class.java)
                    intent.putExtra("qr_code_id", noValue)
                    intent.putExtra("username", username)
                    intent.putExtra("password", password)
                    startActivity(intent)
                    finish()
                } else {
                    // User already exists, handle accordingly
                    Log.d("Response", "Response from server: $response")
                    Toast.makeText(this, "There is an account registered in this Smart Bike Parking System", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "Please use a different Smart Bike Parking System. Thank you.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SignupActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e("Volley Error", error.toString())
                Toast.makeText(this, "An error occurred while checking user existence", Toast.LENGTH_SHORT).show()
            }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["ID"] = noValue.toString()
                return params
            }
        }

        requestQueue.add(stringRequest)
    }

    private fun updateNoAndUserInfo(qrCodeValue: String, noValue: String) {
        updateNoInDatabase(qrCodeValue, noValue)
        updateUserInfoInDatabase(noValue)
    }

    private fun updateNoInDatabase(qrCodeValue: String, noValue: String) {
        val url = "https://nusmb.com/data/updateno.php"
        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener { response ->
                // Handle response from the server
                Log.d("UpdateNoResponse", response)
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e("Volley Error", error.toString())
                Toast.makeText(this, "An error occurred while updating No in database", Toast.LENGTH_SHORT).show()
            }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["username"] = username ?: ""
                params["password"] = password ?: ""
                params["noValue"] = noValue
                return params
            }
        }

        requestQueue.add(stringRequest)
    }

    private fun updateUserInfoInDatabase(noValue: String) {
        val url = "https://nusmb.com/data/updateinfo.php"
        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener { response ->
                // Handle response from the server
                Log.d("UpdateUserInfoResponse", response)
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e("Volley Error", error.toString())
                Toast.makeText(this, "An error occurred while updating User and Pass in database", Toast.LENGTH_SHORT).show()
            }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user"] = username ?: ""
                params["pass"] = password ?: ""
                params["noValue"] = noValue
                return params
            }
        }

        requestQueue.add(stringRequest)
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
}
