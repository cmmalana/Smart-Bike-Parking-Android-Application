package com.example.myapplication

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class ImagesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images)

        // Retrieve the qrCodeId from the Intent
        val qrCodeId = intent.getIntExtra("qr_code_id", -1)

        // Find the WebView by its ID from the XML layout
        val webView: WebView = findViewById(R.id.webView)

        // Enable JavaScript (optional, if required for the website)
        webView.settings.javaScriptEnabled = true

        // Load the web page in the WebView
        webView.loadUrl("https://nusmb.com/images/" + qrCodeId + "/images.php")

        // Set a WebViewClient to handle page navigation inside the WebView
        webView.webViewClient = WebViewClient()
    }
}
