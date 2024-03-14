package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MultipleAccActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images)

        // Retrieve the phone number from the Intent
        val phoneNum = intent.getStringExtra("PhoneNum") ?: ""

        // Find the WebView by its ID from the XML layout
        webView = findViewById(R.id.webView)

        // Enable JavaScript (optional, if required for the website)
        webView.settings.javaScriptEnabled = true

        // Inject JavaScript interface for communication between WebView and Android app
        webView.addJavascriptInterface(WebAppInterface(this, phoneNum), "Android")

        // Load the web page in the WebView
        webView.loadUrl("https://nusmb.com/data/users.php?phone=$phoneNum")

        // Set a WebViewClient to handle page navigation inside the WebView
        webView.webViewClient = MyWebViewClient()
    }

    // JavaScript interface class
    inner class WebAppInterface(private val context: Context, private val phoneNum: String) {

        // JavaScript method to receive user ID from WebView
        @JavascriptInterface
        fun getUserId(userId: Int) {
            // Handle the received user ID here
            val intent = Intent(context, MultipleAcc2Activity::class.java)
            intent.putExtra("UserId", userId)
            intent.putExtra("PhoneNum", phoneNum)
            startActivity(intent)
            finish()
        }

        // Function to be called from JavaScript to send the user ID to Android
        @JavascriptInterface
        fun sendUserIdToAndroid(userId: Int) {
            getUserId(userId)
        }
    }

    // WebViewClient to handle page navigation
    private inner class MyWebViewClient : WebViewClient() {
        // You can override other methods if needed
        // For example: onPageFinished to execute JavaScript after the page loads
    }
}
