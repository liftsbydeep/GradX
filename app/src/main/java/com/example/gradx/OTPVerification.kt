package com.example.gradx

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradx.databinding.ActivityOtpverificationBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OTPVerification : AppCompatActivity() {
    private lateinit var binding: ActivityOtpverificationBinding
    private lateinit var auth: FirebaseAuth
    private var storedVerificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityOtpverificationBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        storedVerificationId = intent.getStringExtra("storedVerificationId")
        auth = FirebaseAuth.getInstance()

//        initViews()
        binding.progressBar3.visibility = View.INVISIBLE

        binding.buttonVerify.setOnClickListener {
            val otp = getEnteredOTP()
            if (otp.length == 6) {
                binding.progressBar3.visibility = View.VISIBLE
                verifyPhoneNumberWithCode(storedVerificationId, otp)
            } else {
                Toast.makeText(this, "Please enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }

        setupOtpInput()
    }

//    private fun initViews() {
//        binding.apply {
//            editTextOTP1 = findViewById(R.id.editTextOTP1)
//            editTextOTP2 = findViewById(R.id.editTextOTP2)
//            editTextOTP3 = findViewById(R.id.editTextOTP3)
//            editTextOTP4 = findViewById(R.id.editTextOTP4)
//            editTextOTP5 = findViewById(R.id.editTextOTP5)
//            editTextOTP6 = findViewById(R.id.editTextOTP6)
//            buttonVerify = findViewById(R.id.buttonVerify)
//            progressBar = findViewById(R.id.progressBar3)
//        }
//    }

    private fun getEnteredOTP(): String {
        return binding.run {
            editTextOTP1.text.toString().trim() +
                    editTextOTP2.text.toString().trim() +
                    editTextOTP3.text.toString().trim() +
                    editTextOTP4.text.toString().trim() +
                    editTextOTP5.text.toString().trim() +
                    editTextOTP6.text.toString().trim()
        }
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        verificationId?.let {
            val credential = PhoneAuthProvider.getCredential(it, code)
            signInWithPhoneAuthCredential(credential)
        } ?: run {
            binding.progressBar3.visibility = View.INVISIBLE
            Toast.makeText(this, "Verification ID is null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = Tasks.await(auth.signInWithCredential(credential))
                withContext(Dispatchers.Main) {
                    binding.progressBar3.visibility = View.INVISIBLE
                    if (result.user != null) {
                        Log.d("success", "signInWithCredential:success")
                        startActivity(Intent(this@OTPVerification, Landing_page::class.java))
                        finish()
                    } else {
                        Log.w("failed", "signInWithCredential:failure")
                        Toast.makeText(this@OTPVerification, "Authentication failed. Please try again.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar3.visibility = View.INVISIBLE
                    handleException(e)
                }
            }
        }
    }

    private fun handleException(exception: Exception?) {
        when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(this, "Authentication failed. Please try again.", Toast.LENGTH_LONG).show()
                exception?.let { Log.e("signInError", it.message.toString()) }
            }
        }
    }

    private fun setupOtpInput() {
        val editTexts = listOf(binding.editTextOTP1, binding.editTextOTP2, binding.editTextOTP3, binding.editTextOTP4, binding.editTextOTP5, binding.editTextOTP6)

        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        if (i < editTexts.size - 1) {
                            editTexts[i + 1].requestFocus()
                        }
                    } else if (s?.length == 0) {
                        if (i > 0) {
                            editTexts[i - 1].requestFocus()
                        }
                    }
                }
            })
        }
    }
}
