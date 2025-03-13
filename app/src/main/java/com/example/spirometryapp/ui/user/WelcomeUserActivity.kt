package com.example.spirometryapp.ui.user

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.spirometryapp.R
import com.example.spirometryapp.ui.login.VerifyEmailActivity

class WelcomeUserActivity : AppCompatActivity() {

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var addressEditText: EditText

    private lateinit var nextButton: Button
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_user_activity)

        // Initialize UI components
        firstNameEditText = findViewById(R.id.etFirstName)
        lastNameEditText = findViewById(R.id.etLastName)
        emailEditText = findViewById(R.id.etEmail)
        phoneEditText = findViewById(R.id.etPhoneNumber)
        addressEditText = findViewById(R.id.etAddress)
        nextButton = findViewById(R.id.btnNext)
        backButton = findViewById(R.id.btnBack)


        // Back button logic
        backButton.setOnClickListener {
            navigateToVerifyEmail()
        }

        // Populate fields with data from VerifyEmailActivity intent
        intent.getStringExtra("firstName")?.let { firstNameEditText.setText(it) }
        intent.getStringExtra("lastName")?.let { lastNameEditText.setText(it) }
        intent.getStringExtra("email")?.let { emailEditText.setText(it) }
        intent.getStringExtra("phoneNumber")?.let { phoneEditText.setText(it) }
        intent.getStringExtra("address")?.let { addressEditText.setText(it) }

        //Other details from VerifyEmailActivity & ForgotPasswordActivity
        intent.getStringExtra("birthDate")
        intent.getStringExtra("gender")
        intent.getStringExtra("height")
        intent.getStringExtra("weight")

        // Next button logic
        nextButton.setOnClickListener {
            navigateToUpdateDetails()
        }
    }

    private fun navigateToVerifyEmail() {
        val email = emailEditText.text.toString().trim()
        val verifyEmailIntent = Intent(this, VerifyEmailActivity::class.java).apply {
            putExtra("email", email)
        }
        startActivity(verifyEmailIntent)
        finish()
    }

    private fun navigateToUpdateDetails() {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val gender = intent.getStringExtra("gender") ?: ""
        val birthDate = intent.getStringExtra("birthDate") ?: ""
        val height = intent.getStringExtra("height") ?: ""
        val weight = intent.getStringExtra("weight") ?: ""

        val updateDetailsIntent = Intent(this, UpdateDetailsActivity::class.java).apply {
            putExtra("firstName", firstName)
            putExtra("lastName", lastName)
            putExtra("email", email)
            putExtra("phoneNumber", phone)
            putExtra("address", address)
            putExtra("birthDate", birthDate)
            putExtra("gender", gender)
            putExtra("height", height)
            putExtra("weight", weight)
        }
        startActivity(updateDetailsIntent)
        finish()
    }

}
