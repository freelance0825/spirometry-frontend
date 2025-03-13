package com.example.spirometryapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.spirometryapp.R
import com.google.android.material.button.MaterialButton

class LoginEmailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_email_activity)

        // Find UI Elements
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnNext = findViewById<MaterialButton>(R.id.btnNext)

        // Handle Back Button Click
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Handle Next Button Click
        btnNext.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isNotEmpty()) {
                // Navigate to VerifyEmailActivity (Placeholder)
                val intent = Intent(this, VerifyEmailActivity::class.java)
                intent.putExtra("email", email)
                startActivity(intent)
            } else {
                etEmail.error = "Enter your email"
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
