package com.example.spirometryapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.spirometryapp.R
import com.example.spirometryapp.databinding.HomeActivityBinding
import com.example.spirometryapp.ui.login.LoginEmailActivity
import com.example.spirometryapp.ui.login.VerifyEmailActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: HomeActivityBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001  // Request code for Google sign-in

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Set up Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Retrieve Web Client ID from strings.xml
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize the binding
        binding = HomeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle Google Login
        binding.btnGoogleLogin.setOnClickListener {
            Log.d("HomeActivity", "Google login clicked")
            signOutGoogle()  // Sign out the user first to start a fresh login flow
            signInWithGoogle()
        }

        // Handle Email Login
        binding.btnEmailLogin.setOnClickListener {
            Log.d("HomeActivity", "Email login clicked")
            navigateToLogin()
        }
    }

    private fun signOutGoogle() {
        // Sign out the user before triggering the Google sign-in flow
        googleSignInClient.signOut().addOnCompleteListener(this) {
            Log.d("HomeActivity", "Signed out from Google.")
        }
    }

    private fun signInWithGoogle() {
        // Start the Google sign-in intent
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN) // This will trigger the Google Sign-In flow
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the intent from GoogleSignInClient.getSignInIntent
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                // Google sign-in failed, handle the error
                Log.w("HomeActivity", "Google sign-in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        // Get the Google ID token
        val idToken = account.idToken
        if (idToken != null) {
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            // Sign in with Firebase Auth
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, navigate to VerifyEmailActivity
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        // If sign-in fails, display a message to the user
                        Log.w("HomeActivity", "signInWithCredential:failure", task.exception)
                        updateUI(null)
                    }
                }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // User is signed in, navigate to VerifyEmailActivity
            Log.d("HomeActivity", "User signed in: ${user.displayName}")
            // Prepare the intent and pass the user's email as an extra
            val intent = Intent(this, VerifyEmailActivity::class.java)
            intent.putExtra("email", user.email) // Pass the email as an extra
            startActivity(intent)
            // Optionally, you can call finish() if you want to prevent the user from navigating back to HomeActivity
        } else {
            Log.d("HomeActivity", "Sign-in failed")
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
