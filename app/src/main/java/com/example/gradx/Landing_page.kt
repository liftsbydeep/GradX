package com.example.gradx

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.gradx.databinding.ActivityLandingPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Landing_page : AppCompatActivity() {
    private lateinit var binding: ActivityLandingPageBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Assuming this is a custom extension function for edge-to-edge display
        binding = ActivityLandingPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        // Handle button click to sign out and navigate to Login_Page
        binding.button.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, Login_Page::class.java))
            finish() // Close Landing_page activity after sign out
        }
    }
}
