package com.example.gradx

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class UpdatePassword : AppCompatActivity() {
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var changePasswordButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_update_password)

        newPasswordEditText = findViewById(R.id.newpass)
        confirmPasswordEditText = findViewById(R.id.cnfnewpass)
        changePasswordButton = findViewById(R.id.changePasswordButton)
        progressBar = findViewById(R.id.progressBar4)

        changePasswordButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (newPassword == confirmPassword) {
                    progressBar.visibility = ProgressBar.VISIBLE
                    updatePassword(newPassword)
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            } else {
                Toast.makeText(this, "Please enter a new password and confirm it", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
        }

    }

    private fun updatePassword(newPassword: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.updatePassword(newPassword)
            ?.addOnCompleteListener { task ->
                progressBar.visibility = ProgressBar.GONE
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    startActivity(Intent(this, Login_Page::class.java))
                    finish() // Finish the activity after successful password change
                } else {
                    Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            }
    }
}
