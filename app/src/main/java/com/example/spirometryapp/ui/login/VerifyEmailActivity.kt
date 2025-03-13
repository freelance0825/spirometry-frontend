package com.example.spirometryapp.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.spirometryapp.R
import com.example.spirometryapp.ui.register.RegisterEmailActivity
import com.example.spirometryapp.ui.user.WelcomeUserActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class VerifyEmailActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvShowPassword: TextView
    private lateinit var btnBack: ImageView
    private var isPasswordVisible = false
    private val client = OkHttpClient()

    companion object {
        private const val BASE_URL = "http://10.0.2.2:8080/api"
        private const val LOGIN_URL = "$BASE_URL/auth/login"
        private const val PROFILE_URL = "$BASE_URL/user/profile"
        private const val ERROR_LOGIN_FAILED = "Login failed. Please try again."
        private const val ERROR_USER_NOT_FOUND = "User not found. Redirecting to registration."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verify_email_activity)

        // Initialize views
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvShowPassword = findViewById(R.id.tvShowPassword)
        btnBack = findViewById(R.id.btnBack)

        // Set email from previous activity (if provided)
        intent.getStringExtra("email")?.let { etEmail.setText(it) }

        // Set listeners
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        btnLogin.setOnClickListener { if (validateInputs()) attemptLogin() }
        tvShowPassword.setOnClickListener { togglePasswordVisibility() }
        tvForgotPassword.setOnClickListener { handleForgotPassword() }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        etPassword.transformationMethod = if (isPasswordVisible) {
            HideReturnsTransformationMethod.getInstance()
        } else {
            PasswordTransformationMethod.getInstance()
        }
        tvShowPassword.text = if (isPasswordVisible) "HIDE" else "SHOW"
        etPassword.setSelection(etPassword.text.length)
    }

    private fun validateInputs(): Boolean {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        return when {
            email.isEmpty() -> showError(etEmail, "Email is required")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> showError(etEmail, "Enter a valid email address")
            password.isEmpty() -> showError(etPassword, "Password is required")
            password.length < 6 -> showError(etPassword, "Password must be at least 6 characters")
            else -> true
        }
    }

    private fun showError(field: EditText, message: String): Boolean {
        field.error = message
        return false
    }

    private fun handleForgotPassword() {
        val email = etEmail.text.toString().trim()
        if (email.isEmpty()) {
            showErrorToast("Please enter your email")
            etEmail.error = "Enter your email"
        } else {
            redirectToActivity(ForgotPasswordActivity::class.java, email)
        }
    }

    private fun attemptLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        val jsonBody = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val request = Request.Builder()
            .url(LOGIN_URL)
            .post(RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), jsonBody.toString()))
            .build()

        handleApiCall(request, ::handleLoginResponse, ERROR_LOGIN_FAILED)
    }

    private fun handleLoginResponse(response: Response) {
        if (!response.isSuccessful) {
            handleErrorResponse(response)
            return
        }

        response.body?.string()?.let { responseBody ->
            val jsonResponse = JSONObject(responseBody)
            val token = jsonResponse.optString("token", "")

            if (token.isNotEmpty()) {
                saveToken(token)
                fetchUserProfile(token)
            } else {
                showErrorToast(ERROR_USER_NOT_FOUND)
                redirectToActivity(RegisterEmailActivity::class.java, etEmail.text.toString().trim())
            }
        } ?: showErrorToast("Unexpected error. Please try again.")
    }

    private fun handleErrorResponse(response: Response) {
        val errorMessage = if (response.code == 404) ERROR_USER_NOT_FOUND else ERROR_LOGIN_FAILED
        Log.e("API_ERROR", "Response Code: ${response.code}")

        runOnUiThread {
            showErrorToast(errorMessage)
            if (response.code == 404) {
                redirectToActivity(RegisterEmailActivity::class.java, etEmail.text.toString().trim())
            }
        }
    }

    private fun saveToken(token: String) {
        getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            .edit()
            .putString("auth_token", token)
            .apply()
        Log.d("TOKEN_STORAGE", "Token saved to SharedPreferences")
    }

    private fun fetchUserProfile(token: String) {
        val request = Request.Builder()
            .url(PROFILE_URL)
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()

        handleApiCall(request, ::handleUserProfileResponse, "Failed to fetch user details.")
    }

    private fun handleUserProfileResponse(response: Response) {
        if (!response.isSuccessful) {
            showErrorToast("Failed to retrieve user details.")
            return
        }

        response.body?.string()?.let { responseBody ->
            val jsonResponse = JSONObject(responseBody)

            val userDetails = mapOf(
                "firstName" to jsonResponse.optString("firstName", "N/A"),
                "lastName" to jsonResponse.optString("lastName", "N/A"),
                "email" to jsonResponse.optString("email", "N/A"),
                "phoneNumber" to jsonResponse.optString("phoneNumber", "N/A"),
                "address" to jsonResponse.optString("address", "N/A"),
                "birthDate" to jsonResponse.optString("birthDate", "N/A"),
                "gender" to jsonResponse.optString("gender", "N/A"),
                "height" to jsonResponse.optDouble("height", 0.0),
                "weight" to jsonResponse.optDouble("weight", 0.0)
            )

            redirectToActivity(WelcomeUserActivity::class.java, userDetails)
        }
    }

    private fun handleApiCall(request: Request, successHandler: (Response) -> Unit, errorMessage: String) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", e.message ?: "Unknown error")
                showErrorToast(errorMessage)
            }

            override fun onResponse(call: Call, response: Response) = successHandler(response)
        })
    }

    private fun redirectToActivity(activityClass: Class<*>, extras: Any? = null) {
        val intent = Intent(this, activityClass).apply {
            when (extras) {
                is String -> putExtra("email", extras)
                is Map<*, *> -> extras.forEach { (key, value) -> putExtra(key.toString(), value.toString()) }
            }
        }
        runOnUiThread {
            startActivity(intent)
            finish()
        }
    }

    private fun showErrorToast(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
    }
}
