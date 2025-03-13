package com.example.spirometryapp.ui.login

import android.util.Log
import android.widget.Toast
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.spirometryapp.R
import com.example.spirometryapp.ui.user.WelcomeUserActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var togglePasswordButton: TextView
    private lateinit var toggleConfirmPasswordButton: TextView
    private lateinit var registerButton: Button
    private lateinit var backButton: ImageView

    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password_activity)

        // Initialize UI components
        emailEditText = findViewById(R.id.etEmail)
        passwordEditText = findViewById(R.id.etPassword)
        confirmPasswordEditText = findViewById(R.id.etConfirmPassword)
        togglePasswordButton = findViewById(R.id.tvShowPassword)
        toggleConfirmPasswordButton = findViewById(R.id.tvShowConfirmPassword)
        registerButton = findViewById(R.id.btnRegister)
        backButton = findViewById(R.id.btnBack)

        // Set back button click listener
        backButton.setOnClickListener { navigateToVerifyEmail() }

        // Set email from previous page to populate emailEditText
        intent.getStringExtra("email")?.let { emailEditText.setText(it) }


        // Set click listeners for password visibility toggle
        togglePasswordButton.setOnClickListener { togglePasswordVisibility() }
        toggleConfirmPasswordButton.setOnClickListener { toggleConfirmPasswordVisibility() }

        // Register button click listener
        registerButton.setOnClickListener { updateUserPassword() }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        passwordEditText.inputType =
            if (isPasswordVisible) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        passwordEditText.setSelection(passwordEditText.text.length)
        togglePasswordButton.text = if (isPasswordVisible) "HIDE" else "SHOW"
    }

    private fun toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible
        confirmPasswordEditText.inputType =
            if (isConfirmPasswordVisible) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        confirmPasswordEditText.setSelection(confirmPasswordEditText.text.length)
        toggleConfirmPasswordButton.text = if (isConfirmPasswordVisible) "HIDE" else "SHOW"
    }

    private fun updateUserPassword() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (!validateInput(email, password, confirmPassword)) return

        val token = getTokenFromSharedPrefs()
        if (token.isNullOrEmpty()) {
            showToast("No token found, please login again")
            return
        }

        val requestBody = createJsonRequestBody(email, password)
        val client = createHttpClient()
        val request = createHttpRequest(requestBody, token)

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    logError("Password update failed: ${e.message}")
                    showToast("Password update failed: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    handlePasswordUpdateResponse(response, token)
                }
            }
        })
    }

    private fun handlePasswordUpdateResponse(response: Response, token: String) {
        val responseBody = response.body?.string() ?: ""

        if (response.isSuccessful) {
            logDebug("Password updated successfully: $responseBody")
            showToast("Password update successful!")
            fetchUserProfile(token) // Fetch user details after successful registration
        } else {
            logError("Password update failed: ${response.message}, Body: $responseBody")
            showToast("Error: $responseBody")
        }
    }

    private fun validateInput(email: String, password: String, confirmPassword: String): Boolean {
        return when {
            email.isEmpty() -> emailEditText.setErrorAndReturnFalse("Email is required")
            !Patterns.EMAIL_ADDRESS.matcher(email)
                .matches() -> emailEditText.setErrorAndReturnFalse("Invalid email format")

            password.isEmpty() -> passwordEditText.setErrorAndReturnFalse("Password is required")
            password.length < 6 -> passwordEditText.setErrorAndReturnFalse("Password must be at least 6 characters")
            confirmPassword.isEmpty() -> confirmPasswordEditText.setErrorAndReturnFalse("Confirm Password is required")
            password != confirmPassword -> confirmPasswordEditText.setErrorAndReturnFalse("Passwords do not match")
            else -> true
        }
    }

    private fun navigateToVerifyEmail() {
        startActivity(Intent(this, VerifyEmailActivity::class.java))
        finish()
    }

    private fun getTokenFromSharedPrefs(): String? {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }

    private fun createJsonRequestBody(email: String, password: String): RequestBody {
        val jsonObject = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        return RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonObject.toString()
        )
    }

    private fun createHttpClient(): OkHttpClient {
        val loggingInterceptor =
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        return OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()
    }

    private fun createHttpRequest(requestBody: RequestBody, token: String): Request {
        return Request.Builder()
            .url("http://10.0.2.2:8080/api/auth/update")
            .put(requestBody)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $token")
            .build()
    }

    private fun fetchUserProfile(token: String) {
        val client = createHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:8080/api/user/profile")
            .get()
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    logError("Failed to fetch user profile: ${e.message}")
                    showToast("Failed to fetch user profile")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(responseBody)
                    navigateToWelcomeUser(jsonResponse)
                } else {
                    logError("Failed to fetch user profile: ${response.message}, Body: $responseBody")
                    showToast("Failed to fetch user profile")
                }
            }
        })
    }

    private fun navigateToWelcomeUser(userProfile: JSONObject) {
        val emailFromIntent = intent.getStringExtra("email")
        val emailFromApi = userProfile.optString("email", "")

        // Prioritize email from the previous page, fallback to API response
        val finalEmail = emailFromIntent ?: emailFromApi

        val intent = Intent(this, WelcomeUserActivity::class.java).apply {
            putExtra("firstName", userProfile.optString("firstName", "N/A"))
            putExtra("lastName", userProfile.optString("lastName", "N/A"))
            putExtra("email", finalEmail)
            putExtra("phoneNumber", userProfile.optString("phoneNumber", "N/A"))
            putExtra("address", userProfile.optString("address", "N/A"))
            putExtra("birthDate", userProfile.optString("birthDate", "N/A"))
            putExtra("gender", userProfile.optString("gender", "N/A"))
            putExtra("height", userProfile.optDouble("height", 0.0))
            putExtra("weight", userProfile.optDouble("weight", 0.0))
        }
        startActivity(intent)
        finish()
    }

    /* <------ FOR LOGGING PURPOSES LOGIC STARTS HERE --------> */

    private fun logError(message: String) {
        Log.e("ForgotPasswordActivity", message)
    }

    private fun logDebug(message: String) {
        Log.d("ForgotPasswordActivity", message)
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun EditText.setErrorAndReturnFalse(errorMessage: String): Boolean {
        this.error = errorMessage
        return false
    }
    /* <------ FOR LOGGING PURPOSES LOGIC END HERE --------> */


}
