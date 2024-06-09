package com.example.gradx

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.gradx.databinding.ActivityLandingPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LandingPage : AppCompatActivity() {
    private lateinit var binding: ActivityLandingPageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLandingPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        if (user != null) {
            binding.email.text = user.email // Populate the email field
            loadUserData(user.uid)
        } else {
            // User is not logged in, redirect to login page.
            startActivity(Intent(this, Login_Page::class.java))
            finish()
        }

        binding.logout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this@LandingPage, Login_Page::class.java))
            finish()
        }

        binding.button2.setOnClickListener {
            startActivity(Intent(this@LandingPage, PhoneAuth::class.java))
        }
    }

    private fun loadUserData(userId: String) {
        firestore.collection("USERS").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("Name")

                    if (name != null) {
                        // Display user name in text field
                        binding.name.text = name
                    } else {
                        Log.d("LandingPage", "No name found for user ID: $userId")
                    }
                } else {
                    Log.d("LandingPage", "No user found with ID: $userId")
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                Log.e("LandingPage", "Error getting user data: $exception")
            }
    }
}
