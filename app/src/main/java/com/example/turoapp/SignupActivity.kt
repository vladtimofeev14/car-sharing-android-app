package com.example.turoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.turoapp.models.UserSession
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class SignupActivity : AppCompatActivity()  {
    private lateinit var signupEmailEditText: EditText
    private lateinit var signupPasswordEditText: EditText
    private lateinit var signupFirstNameEditText: EditText
    private lateinit var signupLastNameEditText: EditText
    private lateinit var signupRoleRadioGroup: RadioGroup
    private lateinit var signupButton: Button
    private lateinit var signupGoToLoginTextView: TextView
    private val auth = Firebase.auth
    private val db  = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        bindWidgets()
        wireUpEvents()
    }
    private fun bindWidgets() {
        signupEmailEditText = findViewById(R.id.signup_email_edittext)
        signupPasswordEditText = findViewById(R.id.signup_password_edittext)
        signupFirstNameEditText = findViewById(R.id.signup_first_name_edittext)
        signupLastNameEditText = findViewById(R.id.signup_last_name_edittext)
        signupButton = findViewById(R.id.signup_button)
        signupGoToLoginTextView = findViewById(R.id.go_to_login_textview)
        signupRoleRadioGroup = findViewById(R.id.signup_role_radio_group)
    }

    private fun wireUpEvents() {
        signupButton.setOnClickListener {
            registerUser()
        }

        signupGoToLoginTextView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun registerUser() {
        val email = signupEmailEditText.text.toString().trim()
        val password = signupPasswordEditText.text.toString().trim()
        val firstName = signupFirstNameEditText.text.toString().trim()
        val lastName = signupLastNameEditText.text.toString().trim()
        val selectedRoleId = signupRoleRadioGroup.checkedRadioButtonId

        if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedRoleId == -1) {
            Toast.makeText(this, "Please select your role.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
            return
        }

        val isOwner = (selectedRoleId == R.id.signup_owner_radiobutton)

        //Create a new user in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    val uid = user?.uid

                    //Prepare extra user info to store in Firestore
                    val profileData = hashMapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email,
                        "isOwner" to isOwner,
                    )

                    // Save profile in the "users" collection, document id = uid
                    // Path in Firestore: users/{uid}
                    db.collection("users")
                        .document(uid!!)
                        .set(profileData)
                        .addOnSuccessListener {

                            UserSession.firstName = firstName
                            UserSession.lastName = lastName
                            UserSession.email = email
                            UserSession.uid = uid
                            UserSession.isOwner = isOwner

                            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Failed to save profile: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                else {
                    // If sign up fails (email already in use, weak password, etc.)
                    Toast.makeText( this, "Fail to create", Toast.LENGTH_SHORT).show()

                }
            }
    }
}