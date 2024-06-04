package com.example.gradx

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradx.databinding.ActivityPhoneAuthBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class PhoneAuth : AppCompatActivity() {
    private var storedVerificationId: String? = null
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var binding:ActivityPhoneAuthBinding
    private lateinit var auth: FirebaseAuth
    private val coroutineScope = CoroutineScope(Dispatchers.IO)


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityPhoneAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        // Invalid request
                        showToast("Invalid request")
                    }
                    is FirebaseTooManyRequestsException -> {
                        // SMS quota exceeded
                        showToast("SMS quota exceeded")
                    }
                    is FirebaseAuthMissingActivityForRecaptchaException -> {
                        // reCAPTCHA verification attempted with null Activity
                        showToast("reCAPTCHA verification failed")
                    }
                    else -> {
                        showToast("Verification failed")
                    }
                }
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG, "onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token
                val intent = Intent(this@PhoneAuth, OTPVerification::class.java).apply {
                    putExtra("storedVerificationId", storedVerificationId)
                }
                startActivity(intent)
            }
        }

        binding.button3.setOnClickListener {
            val phoneNumber = binding.editTextPhone.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                startPhoneNumberVerification(phoneNumber)
            } else {
                showToast("Please enter a phone number")
            }
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        runOnUiThread { binding.progressBar2.visibility = View.VISIBLE }
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        coroutineScope.launch {
            runOnUiThread { binding.progressBar2.visibility = View.VISIBLE }
            try {
                val result = withContext(Dispatchers.IO) {
                    auth.signInWithCredential(credential).await()
                }
                withContext(Dispatchers.Main) {
                    if (result.user != null) {
                        Log.d(TAG, "signInWithCredential:success")
                        val intent = Intent(this@PhoneAuth, UpdatePassword::class.java).apply {
                            putExtra("storedVerificationId", storedVerificationId)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Log.w(TAG, "signInWithCredential:failure")
                        showToast("Authentication failed. Please try again.")
                    }
                }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                withContext(Dispatchers.Main) {
                    Log.w(TAG, "signInWithCredential:failure", e)
                    showToast("Invalid verification code")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.w(TAG, "signInWithCredential:failure", e)
                    showToast("Authentication failed. Please try again.")
                }
            }
            finally {
                runOnUiThread { binding.progressBar2.visibility = View.GONE }
            }
        }
    }

    private fun showToast(message: String) {
        runOnUiThread { binding.progressBar2.visibility = View.GONE }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "PhoneAuth"
    }

}
