package com.example.spirometryapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.spirometryapp.databinding.HomeActivityBinding
import com.example.spirometryapp.ui.login.LoginEmailActivity

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: HomeActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Ensure binding is initialized
            binding = HomeActivityBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Handle Google Login
            binding.btnGoogleLogin.setOnClickListener {
                Log.d("HomeActivity", "Google login clicked")
                navigateToLogin()
            }

            // Handle Email Login
            binding.btnEmailLogin.setOnClickListener {
                Log.d("HomeActivity", "Email login clicked")
                navigateToLogin()
            }
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error initializing activity", e)
        }
    }

    private fun navigateToLogin() {
        try {
            val intent = Intent(this, LoginEmailActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error navigating to LoginEmailActivity", e)
        }
    }
}
