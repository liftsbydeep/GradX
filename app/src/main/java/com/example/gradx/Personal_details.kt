package com.example.gradx

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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

class Personal_details : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_details)

        auth = Firebase.auth
        db = Firebase.firestore
        sharedPreferences = getSharedPreferences("GradxPrefs", Context.MODE_PRIVATE)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val qualificationEditText: EditText = findViewById(R.id.workingat)
        val institutionNameEditText: EditText = findViewById(R.id.experience)
        val batchEditText: EditText = findViewById(R.id.workedfor1st)
        val BatchEditText: EditText = findViewById(R.id.workedfor2nd)
        val hometownEditText: EditText = findViewById(R.id.skillset)
        val saveButton: Button = findViewById(R.id.save1)

        saveButton.setOnClickListener {
            val qualification = qualificationEditText.text.toString()
            val institutionName = institutionNameEditText.text.toString()

            val hometown = hometownEditText.text.toString()
            val workedfor1st=batchEditText.text.toString()
            val workedfor2nd=BatchEditText.text.toString()

            if (qualification.isEmpty() || institutionName.isEmpty() ||  hometown.isEmpty() || workedfor1st.isEmpty() || workedfor2nd.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            savePersonalDetails(qualification, institutionName, workedfor1st,workedfor2nd, hometown)
        }
    }

    private fun savePersonalDetails(qualification: String, institutionName: String, workedfor1st: String,workedfor2nd:String, hometown: String) {
        val email = auth.currentUser?.email
        if (email != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    db.collection("USERS").document(email)
                        .update(
                            mapOf(
                                "qualification" to qualification,
                                "institutionName" to institutionName,
                                "workedfor1st" to workedfor1st,
                                "workedfor2nd" to workedfor2nd,
                                "hometown" to hometown
                            )
                        ).await()

                    saveDetailsToSharedPreferences(qualification, institutionName, workedfor1st,workedfor2nd, hometown)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@Personal_details, "Details updated successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@Personal_details,Proffesional_Details::class.java))

                    }
                } catch (exception: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@Personal_details, "Error updating details: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun saveDetailsToSharedPreferences(
        qualification: String,
        institutionName: String,
        workedfor1st: String,
        workedfor2nd: String,
        hometown: String

    ) {
        val editor = sharedPreferences.edit()
        editor.putString("qualification", qualification)
        editor.putString("institutionName", institutionName)
        editor.putString("workedfor1st", workedfor1st)
        editor.putString("workedfor2nd", workedfor2nd)
        editor.putString("hometown", hometown)
        editor.apply()
    }

    private fun enableEdgeToEdge() {
        // Implementation of enableEdgeToEdge
    }
}
