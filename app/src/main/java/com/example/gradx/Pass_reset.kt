package com.example.gradx

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradx.databinding.ActivityPassResetBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class Pass_reset : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var binding: ActivityPassResetBinding
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPassResetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val resetPasswordButton = binding.resetbtn
        progressBar = binding.progressBar2
        auth = FirebaseAuth.getInstance()

        emailEditText = binding.enteremail
        phoneEditText = binding.pnum

        resetPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()

            if (email.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                sendPasswordResetEmail(email)
            } else if (phone.isNotEmpty()) {
                if (isValidE164PhoneNumber(phone)) {
                    progressBar.visibility = View.VISIBLE
                    startPhoneNumberVerification(phone)
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
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to send password reset email", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(
                this@Pass_reset,
                "Failed to send verification code",
                Toast.LENGTH_SHORT
            ).show()
            progressBar.visibility = View.GONE
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            this@Pass_reset.verificationId = verificationId
            progressBar.visibility = View.GONE
            val intent = Intent(this@Pass_reset, OTPVerification::class.java)
            intent.putExtra("verificationId", verificationId)
            startActivity(intent)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    Toast.makeText(this, "Phone authentication successful", Toast.LENGTH_SHORT)
                        .show()
                    progressBar.visibility = View.GONE
                    finish()
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "Invalid verification code", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, "Phone authentication failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                    progressBar.visibility = View.GONE
                }
            }
    }

    private fun isValidE164PhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.matches(Regex("^\\+[1-9]\\d{1,14}\$"))
    }
}
