package com.example.gradx

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserProfile : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_profile)

        db = Firebase.firestore
        auth = Firebase.auth
        progressBar = findViewById(R.id.progressBar7)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val userId = intent.getStringExtra("USER_ID")

        if (userId != null) {
            fetchUserDetails(userId)
        } else {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchUserDetails(userId: String) {
        Log.d("UserProfile", "Fetching details for user with ID: $userId")
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val documentSnapshot = db.collection("USERS").document(userId).get().await()
                if (documentSnapshot.exists()) {
                    val qualification = documentSnapshot.getString("qualification") ?: "N/A"
                    val institutionName = documentSnapshot.getString("institutionName") ?: "N/A"
                    val workedfor1st = documentSnapshot.getString("workedfor1st") ?: "N/A"
                    val workedfor2nd = documentSnapshot.getString("workedfor2nd") ?: "N/A"
                    val hometown = documentSnapshot.getString("hometown") ?: "N/A"
                    val userName = documentSnapshot.getString("Name") ?: "N/A"
                    val userProfileImageUrl = documentSnapshot.getString("profileImageUrl")
                    val workingat=documentSnapshot.getString("workingat")
                    val experience=documentSnapshot.getString("experience")
                    val workedfor=documentSnapshot.getString("workedfor")
                    val skillset=documentSnapshot.getString("skillset")
                    val workcity=documentSnapshot.getString("workcity")
                    val designation=documentSnapshot.getString("designation")

                    withContext(Dispatchers.Main) {
                        if (workingat!= null) {
                            if (experience != null) {
                                if (workedfor != null) {
                                    if (userProfileImageUrl != null) {
                                        if (skillset != null) {
                                            if (workcity != null) {
                                                updateUI(qualification, institutionName, workedfor1st,workedfor2nd, hometown, userName, userProfileImageUrl,workingat,experience,workedfor,skillset,workcity,designation)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@UserProfile, "No details found for this user", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UserProfile, "Error: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun updateUI(
        qualification: String,
        institutionName: String,
        workedfor1st: String,
        workedfor2nd: String,
        hometown: String,
        userName: String,
        userProfileImageUrl: String,
        workingat: String,
        experience: String,
        workedfor: String,
        skillset: String,
        workcity: String,
        designation:String?

    ) {
        findViewById<TextView>(R.id.textView18).text = qualification
        findViewById<TextView>(R.id.textView25).text = institutionName
        findViewById<TextView>(R.id.textView28).text = workedfor1st
        findViewById<TextView>(R.id.textView49).text = workedfor2nd
        findViewById<TextView>(R.id.textView15).text = hometown
        findViewById<TextView>(R.id.textView12).text = userName
        findViewById<TextView>(R.id.textView32).text=workingat
        findViewById<TextView>(R.id.textView34).text=experience
        findViewById<TextView>(R.id.textView36).text=workedfor
        findViewById<TextView>(R.id.textView38).text=skillset
       findViewById<TextView>(R.id.textView19).text=workingat
        findViewById<TextView>(R.id.textView20).text=workcity
        findViewById<TextView>(R.id.textView16).text=designation
        val profileImageView: CircleImageView = findViewById(R.id.profilepic2)
        Glide.with(this@UserProfile)
            .load(userProfileImageUrl)
            .placeholder(R.drawable.baseline_people_alt_24)
            .into(profileImageView)
    }
}