package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class InstructionsActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions)

        // Check if camera and internet permissions are granted
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val internetPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)

        if (cameraPermission == PackageManager.PERMISSION_GRANTED && internetPermission == PackageManager.PERMISSION_GRANTED) {
            // Permissions are granted, continue with app logic
        } else {
            // Request permissions
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.INTERNET),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    fun startSignupActivity(view: View) {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, show a message
                Toast.makeText(this, "Camera and internet permission granted.", Toast.LENGTH_LONG).show()
            } else {
                val message = when {
                    grantResults[0] != PackageManager.PERMISSION_GRANTED && grantResults[1] != PackageManager.PERMISSION_GRANTED -> {
                        // Both camera and internet permissions denied
                        "Camera and internet are needed to make the app run normally. Please install the application or clear its data."
                    }
                    grantResults[0] != PackageManager.PERMISSION_GRANTED -> {
                        // Camera permission denied
                        "Camera is needed to make the app run normally. Please install the application or clear its data."
                    }
                    else -> {
                        // Internet permission denied
                        "Internet is needed to make the app run normally. Please grant the permission or clear the application data."
                    }
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}
