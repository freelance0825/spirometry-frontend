package com.example.spirometryapp.ui.user

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.spirometryapp.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.*

class UpdateDetailsActivity : AppCompatActivity() {

    private lateinit var birthDateEditText: EditText
    private lateinit var tvGender: TextView
    private lateinit var spinnerGender: Spinner
    private lateinit var heightEditText: EditText
    private lateinit var weightEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var backButton: ImageView

    private var isFirstSelection = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.update_details_activity)

        birthDateEditText = findViewById(R.id.etBirthDate)

        tvGender = findViewById(R.id.tvGender)
        spinnerGender = findViewById(R.id.spinnerGender)


        heightEditText = findViewById(R.id.heightInput)
        weightEditText = findViewById(R.id.weightInput)
        submitButton = findViewById(R.id.btnSubmit)
        backButton = findViewById(R.id.btnBack)


        /*  <----- START OF GENDER LOGIC ------> */

        // Load Gender Options from strings.xml
        val genderOptions = resources.getStringArray(R.array.gender_options)

        // Set up Spinner Adapter
        val adapter = ArrayAdapter(this, R.layout.custom_spinner_dropdown, genderOptions)
        spinnerGender.adapter = adapter

        // Initially set hint text
        tvGender.text = "Select Gender"

        // Handle Click on TextView to Show Spinner
        tvGender.setOnClickListener {
            spinnerGender.visibility = View.VISIBLE
            spinnerGender.performClick()
        }

        // Ensure the first selection works correctly
        spinnerGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }
                tvGender.text = genderOptions[position]
                spinnerGender.visibility = View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        /* <------ END OF GENDER LOGIC -----------> */



        // Populate fields with data from WelcomeUserActivity intent
        intent.getStringExtra("birthDate")?.let { birthDateEditText.setText(it) }
        intent.getStringExtra("gender")?.let { tvGender.setText(it) }
        intent.getStringExtra("height")?.let { heightEditText.setText(it) }
        intent.getStringExtra("weight")?.let { weightEditText.setText(it) }

        // Get Input Details from WelcomeUserActivity
        val firstName = intent.getStringExtra("firstName") ?: ""
        val lastName = intent.getStringExtra("lastName") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val phoneNumber = intent.getStringExtra("phoneNumber") ?: ""
        val address = intent.getStringExtra("address") ?: ""

        backButton.setOnClickListener { finish() }
        submitButton.setOnClickListener {
            sendUserDataToBackend(
                firstName,
                lastName,
                email,
                phoneNumber,
                address
            )
        }
        birthDateEditText.setOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate =
                    String.format("%02d-%02d-%04d", selectedMonth + 1, selectedDay, selectedYear)
                birthDateEditText.setText(selectedDate)
            }, year, month, day)

        datePickerDialog.show()
    }


    private fun sendUserDataToBackend(
        firstName: String,
        lastName: String,
        email: String,
        phoneNumber: String,
        address: String
    ) {
        val birthDate = birthDateEditText.text.toString().trim()
        val gender = tvGender.text.toString().trim()
        val height = heightEditText.text.toString().trim()
        val weight = weightEditText.text.toString().trim()

        if (birthDate.isEmpty() || gender.isEmpty() || height.isEmpty() || weight.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Retrieve token from SharedPreferences
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Authentication token is missing", Toast.LENGTH_SHORT).show()
            Log.e("UpdateProfile", "Token not found in SharedPreferences")
        } else {
            Log.d("UpdateProfile", "Retrieved Token: $token")
        }

        val jsonObject = JSONObject().apply {
            put("firstName", firstName)
            put("lastName", lastName)
            put("email", email)
            put("phoneNumber", phoneNumber)
            put("address", address)
            put("birthDate", birthDate)
            put("gender", gender)
            put("height", height)
            put("weight", weight)
        }

        val requestBody = jsonObject.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("http://10.0.2.2:8080/api/user/update/profile")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .put(requestBody)
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("UpdateProfile", "Request failed: ${e.message}")
                    Toast.makeText(
                        this@UpdateDetailsActivity,
                        "Failed to update profile",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    val responseBody = response.body?.string()
                    Log.d("UpdateProfile", "Raw response: ${responseBody ?: "No response body"}")
                    Log.d("UpdateProfile", "Response code: ${response.code}")

                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@UpdateDetailsActivity,
                            "Profile updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Log.e("UpdateProfile", "Update failed: $responseBody")
                        Toast.makeText(
                            this@UpdateDetailsActivity,
                            "Update failed: ${responseBody ?: "Unknown error"}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }
}
