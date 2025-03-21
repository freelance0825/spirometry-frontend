package com.example.spirometryapp.ui.spirometry

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.spirometryapp.R
import com.example.spirometryapp.ui.home.HomeActivity

class SpirometryTestRecord : AppCompatActivity() {

    private lateinit var submitButton: Button
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.spirometry_test_record)

       // Initialize UI
        submitButton = findViewById(R.id.btnSubmit)
        backButton = findViewById(R.id.btnBack)


        // Back Button Logic
        backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Submit Button Logic
        submitButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }


    }
}