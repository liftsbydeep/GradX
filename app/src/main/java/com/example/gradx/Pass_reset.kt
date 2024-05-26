package com.example.gradx

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradx.databinding.ActivityPassResetBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class Pass_reset : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var binding: ActivityPassResetBinding
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPassResetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val resetPasswordButton = binding.resetbtn

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance()

        // Find email EditText
        emailEditText = binding.enteremail

        // Find phone EditText
        phoneEditText = binding.pnum

        // Set click listener for the reset password button
        resetPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()

            if (email.isNotEmpty()) {
                // Reset password using email
                sendPasswordResetEmail(email)
            } else if (phone.isNotEmpty()) {
                // Reset password using phone number
                if (isValidE164PhoneNumber(phone)) {
                    sendVerificationCode(phone)
                } else {
                    Toast.makeText(
                        this,
                        "Please enter a valid phone number in E.164 format",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Please enter your email address or phone number",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to send password reset email", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            this,
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(
                        this@Pass_reset,
                        "Failed to send verification code",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@Pass_reset.verificationId = verificationId
                    // Proceed to OTP verification
                    // You can navigate to a new activity for OTP verification or show a dialog here
                    // For simplicity, let's assume you navigate to a new activity
                    val intent = Intent(this@Pass_reset, com.example.gradx.OTPVerification::class.java)
                    intent.putExtra("verificationId", verificationId)
                    startActivity(intent)
                }
            })
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Phone authentication successful, allow user to reset password
                    Toast.makeText(this, "Phone authentication successful", Toast.LENGTH_SHORT)
                        .show()
                    // Navigate to password reset screen
                    finish()
                } else {
                    // Phone authentication failed
                    Toast.makeText(this, "Phone authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun isValidE164PhoneNumber(phoneNumber: String): Boolean {
        // Check if the phone number starts with a '+' and contains only digits afterward
        return phoneNumber.matches(Regex("^\\+[1-9]\\d{1,14}\$"))
    }


}