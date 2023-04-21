package com.example.register_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        main()
    }

    private fun main() {
        // get the message text field
        val messageField = findViewById<TextView>(R.id.messageTxt)

        // get the uid from the intent
        val uid = intent.getStringExtra("uid")

        // get the user data from the db by uid
        val db = Firebase.firestore;

        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // get email
                        val email = document.data?.get("email")

                        // get display name
                        val displayName = document.data?.get("displayName")

                        // get timestamp & format it
                        var timestamp = document.data?.get("timestamp") as Long
                        var date = Date(timestamp)
                        val format = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                        val formatedTimestamp = format.format(date)

                        messageField.text = "Hi there ${displayName},\n\nEmail: ${email}\nTimestamp: ${formatedTimestamp}"
                    } else {
                        // set the message text field to an error message
                        messageField.text = "Could not get user data"
                    }
                }
                .addOnFailureListener { exception ->
                    // set the message text field to an error message
                    messageField.text = "Error: ${exception}"
                }
        }

        // get the logout button
        val logoutButton = findViewById<TextView>(R.id.logoutBtn)

        logoutButton.setOnClickListener {
            // firebase logout
            val auth = FirebaseAuth.getInstance()
            auth.signOut()

            // go to main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}