package com.example.turoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.turoapp.models.UserSession
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {
    private lateinit var loginEmailEditText: EditText
    private lateinit var loginPasswordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var loginGoToSignupTextView: TextView
    private val db = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)
        bindWidgets()
        wireUpEvents()
    }

    private fun bindWidgets() {
        loginEmailEditText = findViewById(R.id.login_email_edittext)
        loginPasswordEditText = findViewById(R.id.login_password_edittext)
        loginButton = findViewById(R.id.login_button)
        loginGoToSignupTextView = findViewById(R.id.go_to_signup_textview)
    }

    private fun wireUpEvents() {
        loginButton.setOnClickListener {
            loginUser()
        }

        loginGoToSignupTextView.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun redirectUser() {
        val isOwner = UserSession.isOwner

        if (isOwner == true) {
            // Redirect to Owner dashboard
            val intent = Intent(this, MyListingsActivity::class.java)
            startActivity(intent)
        } else {
            // Redirect to Renter dashboard
            val intent = Intent(this, SearchCarActivity::class.java)
            startActivity(intent)
        }

        finish() // Close MainActivity so user can't go back
    }

    private fun loginUser() {
        val email = loginEmailEditText.text.toString().trim()
        val password = loginPasswordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
            return
        }

        // Ask Firebase to authenticate the user
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //Get current logged-in user and all the data
                    //Assign said data to UserSession
                    val currentUid = auth.currentUser?.uid
                    db.collection("users")
                        .document(currentUid!!)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {

                                UserSession.firstName = document.getString("firstName") ?: ""
                                UserSession.lastName = document.getString("lastName") ?: ""
                                UserSession.email = document.getString("email") ?: ""
                                UserSession.uid = currentUid
                                UserSession.isOwner = document.getBoolean("isOwner")

                                //need to put finish() inside this block of code
                                Toast.makeText(this, "You have been logged-in", Toast.LENGTH_SHORT).show()
                                redirectUser()


                            } else {
                                Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d("MainActivity:Error", "get failed with ", exception)
                            Toast.makeText(this, "Error loading user.", Toast.LENGTH_SHORT).show()
                            finish()
                        }

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithEmail:failure", task.exception)
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()

                }
            }
    }
}