package com.example.register_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main()
    }

    private fun login() {
        // go to login activity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun main() {
        // get the emailField and passwordField values
        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val displayNameField = findViewById<EditText>(R.id.displayNameField)

        // get the reg btn
        val registerButton = findViewById<Button>(R.id.registerBtn)

        // get the error text field
        val errorText = findViewById<TextView>(R.id.registerErrorTxt)


        registerButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            val displayName = displayNameField.text.toString()

            // check if the email and password are not empty
            if (email.isNotEmpty() && password.isNotEmpty() && displayName.isNotEmpty()) {

                // check if display name is at least 3 characters
                if (displayName.length >= 3) {

                    // check if password is at least 8 characters, contains a number and a capital letter and a special character
                    if (password.length >= 8 && password.contains(Regex("[0-9]")) && password.contains(Regex("[A-Z]")) && password.contains(Regex("[^A-Za-z0-9]"))) {

                        // firebase create user
                        val auth = FirebaseAuth.getInstance()

                        // create db connection to link user to db
                        val db = Firebase.firestore;

                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // User created successfully
                                    val user = auth.currentUser

                                    // add user to db
                                    val userMap = hashMapOf(
                                        "displayName" to displayName,
                                        "email" to email,
                                        "timestamp" to System.currentTimeMillis()
                                    )
                                    db.collection("users").document(user?.uid.toString())
                                        .set(userMap).addOnFailureListener { exception ->
                                        val errorMessage = exception.message
                                        errorText.text = "User creation failed: $errorMessage"
                                    }

                                    // send email verification
                                    user?.sendEmailVerification()
                                        ?.addOnCompleteListener { verificationTask ->
                                            if (verificationTask.isSuccessful) {
                                                // return user to login activity
                                                login()
                                            } else {
                                                // email verification sending failed
                                                val message = verificationTask.exception?.message
                                                errorText.text = (message)
                                            }
                                        }
                                } else {
                                    // User creation failed
                                    val message = task.exception?.message
                                    errorText.text = (message)
                                }
                            }
                    } else {
                        // password is not valid
                        errorText.text = ("Password must be at least 8 characters, contain a number, a capital letter and a special character")
                    }
                } else {
                    // display name is not valid
                    errorText.text = ("Display name must be at least 3 characters")
                }
            } else {
                // email or password is empty
                errorText.text = ("Email, Display name or password field is empty")
            }
        }

        val loginTextBtn = findViewById<TextView>(R.id.loginTextBtn)
        loginTextBtn.setOnClickListener {
            login()
        }
    }
}