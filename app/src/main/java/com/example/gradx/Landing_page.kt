package com.example.gradx

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gradx.databinding.ActivityLandingPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

        val email = intent.getStringExtra("USER_EMAIL")
        if (email != null) {
            binding.email.text = email // Populate the email field
            loadUserData(email)
        } else {
            // User email is not available, redirect to login page.
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

    private fun loadUserData(email: String) {
        lifecycleScope.launch {
            try {
                val documents = firestore.collection("USERS").whereEqualTo("Email", email).get().await()
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val name = document.getString("Name")

                    if (name != null) {
                        binding.name.text = name
                    } else {
                        Log.d("LandingPage", "No name found for email: $email")
                    }
                } else {
                    Log.d("LandingPage", "No user found with email: $email")
                }
            } catch (exception: Exception) {
                // Handle any errors
                Log.e("LandingPage", "Error getting user data: $exception")
            }
        }
    }
}
