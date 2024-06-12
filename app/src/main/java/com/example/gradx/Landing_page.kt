package com.example.gradx

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gradx.databinding.ActivityLandingPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LandingPage : AppCompatActivity() {
    private lateinit var binding: ActivityLandingPageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLandingPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        sharedPreferences = getSharedPreferences("GradxPrefs", Context.MODE_PRIVATE)

        // Check login status
        checkLoginStatus()

        // Setup bottom navigation
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> FragmentHandler(home())
                R.id.connect -> FragmentHandler(connect())
                R.id.profile -> FragmentHandler(Profile())
                else -> {
                    showFragment(home())
                }
            }
            true
        }

        // Show the home fragment by default
        if (savedInstanceState == null) {
            binding.bottomNavigationView.selectedItemId = R.id.home
        }
    }

    private fun checkLoginStatus() {
        val isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false)
        if (!isLoggedIn || auth.currentUser == null) {
            // User is not logged in, redirect to login page
            startActivity(Intent(this, Login_Page::class.java))
            finish()
        }
    }

    private fun FragmentHandler(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }

    private fun showFragment(fragment: Fragment): Boolean {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
        return true
    }
}
