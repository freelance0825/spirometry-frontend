package com.example.spirometryapp.ui.register

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.spirometryapp.R
import com.example.spirometryapp.ui.user.WelcomeUserActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class RegisterEmailActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var togglePasswordButton: TextView
    private lateinit var toggleConfirmPasswordButton: TextView
    private lateinit var registerButton: Button
    private lateinit var backButton: ImageView
    private lateinit var sharedPreferences: SharedPreferences

    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_email_activity)
        initializeUI()
        loadEmailFromIntent()
        setClickListeners()
    }

    private fun initializeUI() {
        emailEditText = findViewById(R.id.etEmail)
        passwordEditText = findViewById(R.id.etPassword)
        confirmPasswordEditText = findViewById(R.id.etConfirmPassword)
        togglePasswordButton = findViewById(R.id.tvShowPassword)
        toggleConfirmPasswordButton = findViewById(R.id.tvShowConfirmPassword)
        registerButton = findViewById(R.id.btnRegister)
        backButton = findViewById(R.id.btnBack)
        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    }

    private fun loadEmailFromIntent() {
        intent.getStringExtra("email")?.let { emailEditText.setText(it) }
    }

    private fun setClickListeners() {
        togglePasswordButton.setOnClickListener { togglePasswordVisibility() }
        toggleConfirmPasswordButton.setOnClickListener { toggleConfirmPasswordVisibility() }
        registerButton.setOnClickListener { registerUser() }
        backButton.setOnClickListener { finish() }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        updatePasswordVisibility(passwordEditText, togglePasswordButton, isPasswordVisible)
    }

    private fun toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible
        updatePasswordVisibility(confirmPasswordEditText, toggleConfirmPasswordButton, isConfirmPasswordVisible)
    }

    private fun updatePasswordVisibility(editText: EditText, toggleButton: TextView, isVisible: Boolean) {
        editText.inputType = if (isVisible) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        editText.setSelection(editText.text.length)
        toggleButton.text = if (isVisible) "HIDE" else "SHOW"
    }

    private fun registerUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (!validateInput(email, password, confirmPassword)) return
        sendRegistrationRequest(email, password)
    }

    private fun sendRegistrationRequest(email: String, password: String) {
        val jsonObject = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonObject.toString()
        )

        val request = Request.Builder()
            .url("http://10.0.2.2:8080/api/auth/register")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showToast("Registration failed: ${e.message}")
                Log.e("RegisterError", "Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        handleSuccessfulResponse(response.body?.string())
                    } else {
                        showToast("Error: ${response.message}")
                        Log.e("RegisterError", "Error: ${response.message}")
                    }
                }
            }
        })
    }

    private fun handleSuccessfulResponse(responseBody: String?) {
        try {
            val jsonResponse = JSONObject(responseBody ?: "")
            val token = jsonResponse.optString("token", "")
            if (token.isNotEmpty()) {
                saveToken(token)
                showToast("Registration successful!")
                navigateToWelcomeUser()
            } else {
                showToast("Token missing in response!")
            }
        } catch (e: Exception) {
            Log.e("RegisterError", "JSON parsing error: ${e.message}")
            showToast("Invalid server response")
        }
    }

    private fun saveToken(token: String) {
        sharedPreferences.edit().putString("auth_token", token).apply()
        Log.d("RegisterSuccess", "Token saved successfully")
    }

    private fun validateInput(email: String, password: String, confirmPassword: String): Boolean {
        return when {
            email.isEmpty() -> emailEditText.showError("Email is required")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> emailEditText.showError("Invalid email format")
            password.isEmpty() -> passwordEditText.showError("Password is required")
            password.length < 6 -> passwordEditText.showError("Password must be at least 6 characters")
            confirmPassword.isEmpty() -> confirmPasswordEditText.showError("Confirm Password is required")
            password != confirmPassword -> confirmPasswordEditText.showError("Passwords do not match")
            else -> true
        }
    }

    private fun EditText.showError(message: String): Boolean {
        this.error = message
        return false
    }

    private fun showToast(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
    }

    private fun navigateToWelcomeUser() {
        Intent(this, WelcomeUserActivity::class.java).apply {
            putExtra("email", emailEditText.text.toString().trim())
        }.also {
            startActivity(it)
            finish()
        }
    }
}