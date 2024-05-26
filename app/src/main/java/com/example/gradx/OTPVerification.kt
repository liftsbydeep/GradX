package com.example.gradx

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider

class OTPVerification : AppCompatActivity() {
    private lateinit var editTextOTP1: EditText
    private lateinit var editTextOTP2: EditText
    private lateinit var editTextOTP3: EditText
    private lateinit var editTextOTP4: EditText
    private lateinit var editTextOTP5: EditText
    private lateinit var editTextOTP6: EditText
    private lateinit var buttonVerify: Button

    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_otpverification)

        // Get verification ID from intent
        verificationId = intent.getStringExtra("verificationId")

        editTextOTP1 = findViewById(R.id.editTextOTP1)
        editTextOTP2 = findViewById(R.id.editTextOTP2)
        editTextOTP3 = findViewById(R.id.editTextOTP3)
        editTextOTP4 = findViewById(R.id.editTextOTP4)
        editTextOTP5 = findViewById(R.id.editTextOTP5)
        editTextOTP6 = findViewById(R.id.editTextOTP6)
        buttonVerify = findViewById(R.id.buttonVerify)

        // Add text change listeners to automatically shift cursor to next EditText
        editTextOTP1.addTextChangedListener(OTPTextWatcher(editTextOTP1, editTextOTP2))
        editTextOTP2.addTextChangedListener(OTPTextWatcher(editTextOTP2, editTextOTP3))
        editTextOTP3.addTextChangedListener(OTPTextWatcher(editTextOTP3, editTextOTP4))
        editTextOTP4.addTextChangedListener(OTPTextWatcher(editTextOTP4, editTextOTP5))
        editTextOTP5.addTextChangedListener(OTPTextWatcher(editTextOTP5, editTextOTP6))

        buttonVerify.setOnClickListener {
            val otp1 = editTextOTP1.text.toString().trim()
            val otp2 = editTextOTP2.text.toString().trim()
            val otp3 = editTextOTP3.text.toString().trim()
            val otp4 = editTextOTP4.text.toString().trim()
            val otp5 = editTextOTP5.text.toString().trim()
            val otp6 = editTextOTP6.text.toString().trim()
            val otp = otp1 + otp2 + otp3 + otp4 + otp5 + otp6

            if (otp.length == 6) {
                // Verify OTP
                verifyOTP(otp)
            } else {
                Toast.makeText(this, "Please enter complete OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyOTP(otp: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // OTP verification successful
                    Toast.makeText(this, "OTP verified successfully", Toast.LENGTH_SHORT).show()
                    // Navigate to password reset activity
                    val intent = Intent(this, UpdatePassword::class.java)
                    startActivity(intent)
                    finish() // Finish OTP verification activity
                } else {
                    // OTP verification failed
                    Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private class OTPTextWatcher(private val currentEditText: EditText, private val nextEditText: EditText) :
        TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (s?.length == 1) {
                nextEditText.requestFocus()
            }
        }
    }
}
