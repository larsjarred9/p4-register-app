package com.example.register_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        main()
    }

    private fun dashboard(uid: String) {
        // go to dashboard activity
        val intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra("uid", uid)
        startActivity(intent)
        finish()
    }

    private fun register() {
        // go to register (main) activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun main() {
        // get the emailField and passwordField values
        val emailField = findViewById<EditText>(R.id.emailFieldLogin)
        val passwordField = findViewById<EditText>(R.id.passwordFieldLogin)

        // get the login btn
        val loginButton = findViewById<TextView>(R.id.loginBtn)

        // get the error text field
        val errorText = findViewById<TextView>(R.id.loginErrorTxt)

        // set attempt to 0
        var attempt = 0;

        loginButton.setOnClickListener {
            // check if the email and password fields do not contain empty values
            if (emailField.text.isNotEmpty() && passwordField.text.isNotEmpty()) {

                attempt++;
                // firebase login
                val auth = FirebaseAuth.getInstance()

                // Firebase firestore db
                val db = Firebase.firestore;

                // check if user is blocked in db

                // get user from db with email
                db.collection("users")
                    .whereEqualTo("email", emailField.text.toString())
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot) {

                            // check if document.data["blocked"] is not null or empty
                            if (document.data["blocked"] != null && document.data["blocked"].toString().isNotEmpty()) {
                                // user is blocked
                                errorText.text = "You have been blocked for unusual activity on your account."
                            } else {
                                // user is not blocked
                                auth.signInWithEmailAndPassword(
                                    emailField.text.toString(),
                                    passwordField.text.toString()
                                ).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // User logged in successfully
                                        val user = auth.currentUser
                                        if (user != null) {
                                            if (user.isEmailVerified) {
                                                // email is verified
                                                dashboard(user.uid)
                                            } else {
                                                // email is not verified
                                                errorText.text = "Please verify by clicking the link sent to your email"
                                            }
                                        }
                                    } else {
                                        // User login failed
                                        val message = task.exception?.message
                                        errorText.text = message
                                    }
                                }.addOnFailureListener { exception ->
                                    if(attempt > 3) {
                                        // get user from db with email to update blocked field
                                        db.collection("users")
                                            .whereEqualTo("email", emailField.text.toString())
                                            .get()
                                            .addOnSuccessListener { querySnapshot ->
                                                for (document in querySnapshot) {
                                                    db.collection("users").document(document.id).update("blocked", System.currentTimeMillis())
                                                }
                                                errorText.text = "You have been blocked for unusual activity on your account."
                                            }
                                    } else {
                                        errorText.text = "Het wachtwoord is onjuist je hebt nog " + (3 - attempt) + " pogingen over"
                                    }
                                }
                            }
                        }
                    } .addOnFailureListener { exception ->
                        errorText.text = exception.message
                    }
            } else {
                // email or password field is empty
                errorText.text = "Please enter your email and password"
            }
        }

        val registerTextBtn = findViewById<TextView>(R.id.registerTextBtn)
        registerTextBtn.setOnClickListener {
            register()
        }
    }
}