package com.example.gradx

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Proffesional_Details : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_proffesional_details)
        auth = Firebase.auth
        db = Firebase.firestore
        sharedPreferences = getSharedPreferences("GradxPrefs", Context.MODE_PRIVATE)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val working_at: EditText = findViewById(R.id.workingat1)
        val experience: EditText = findViewById(R.id.experience1)
        val worked_for: EditText = findViewById(R.id.workedfor1)
        val skill_set: EditText = findViewById(R.id.skillset1)
        val saveButton: Button = findViewById(R.id.save1)
        val work_city:EditText=findViewById(R.id.textView45)
        val designation:EditText=findViewById(R.id.textView50)
        saveButton.setOnClickListener {
            val workingat = working_at.text.toString()
            val experience = experience.text.toString()
            val workedfor = worked_for.text.toString()
            val skillset = skill_set.text.toString()
           val wokcity=work_city.text.toString()
            val designation=designation.text.toString()
            if (workingat.isEmpty() || experience.isEmpty() || workedfor.isEmpty() || skillset.isEmpty() || wokcity.isEmpty() || designation.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            savePersonalDetails(workingat, experience,workedfor,skillset,wokcity,designation)
        }
    }
    private fun savePersonalDetails(
        workingat: String,
        experience: String,
        workedfor: String,
        skillset: String,
        workcity: String,
        designation: String,) {
        val email = auth.currentUser?.email
        if (email != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    db.collection("USERS").document(email)
                        .update(
                            mapOf(
                                "workingat" to workingat,
                                "experience" to experience,
                                "workedfor" to workedfor,
                                "skillset" to skillset,
                                "workcity" to workcity,
                                "designation" to designation
                            )
                        ).await()



                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@Proffesional_Details, "Details updated successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@Proffesional_Details,LandingPage::class.java))
                        finish()

                    }
                } catch (exception: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@Proffesional_Details, "Error updating details: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


}