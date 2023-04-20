package com.example.register_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

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
        var attempt = 0;
        // get the emailField and passwordField values
        val emailField = findViewById<EditText>(R.id.emailFieldLogin)
        val passwordField = findViewById<EditText>(R.id.passwordFieldLogin)

        // get the login btn
        val loginButton = findViewById<TextView>(R.id.loginBtn)

        // get the error text field
        val errorText = findViewById<TextView>(R.id.loginErrorTxt)


        loginButton.setOnClickListener {
            // check if the email and password fields do not contain empty values
            if (emailField.text.isNotEmpty() && passwordField.text.isNotEmpty()) {

                attempt++;
                // firebase login
                val auth = FirebaseAuth.getInstance()

                auth.signInWithEmailAndPassword(
                    emailField.text.toString(),
                    passwordField.text.toString()
                )
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // User logged in successfully
                            val user = auth.currentUser
                            if (user != null) {
                                if (user.isEmailVerified) {
                                    if(attempt > 3) {
                                        errorText.setText("You have exceeded the number of 3 attempts")
                                    } else {
                                        // email is verified
                                        dashboard(user.uid)
                                    }
                                } else {
                                    // email is not verified
                                    errorText.setText("Please verify by clicking the link sent to your email")
                                }
                            }
                        } else {
                            // User login failed
                            val message = task.exception?.message
                            errorText.setText(message)
                        }
                    }
            } else {
                // email or password field is empty
                errorText.setText("Please enter your email and password")
            }
        }

        val registerTextBtn = findViewById<TextView>(R.id.registerTextBtn)
        registerTextBtn.setOnClickListener {
            register()
        }
    }
}