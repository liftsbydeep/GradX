package com.example.gradx

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.gradx.databinding.ActivityLoginPageBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class Login_Page : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var progressBar: ProgressBar
    private lateinit var binding: ActivityLoginPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.pass.setupPasswordVisibilityToggle()

        // Call setupPasswordVisibilityToggle() for cnfpasss EditText
        binding.emailll.setupPasswordVisibilityToggle()
        progressBar = binding.progressBar
        auth = FirebaseAuth.getInstance()

        binding.signup.setOnClickListener {
            startActivity(Intent(this, Signup_Page::class.java))
        }


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googlelogin.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            signInWithGoogle()
        }

        binding.login.setOnClickListener {
            if (check()) {
                progressBar.visibility = View.VISIBLE
                val email = binding.emailll.text.toString().trim()
                val password = binding.pass.text.toString().trim()
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        progressBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            startActivity(
                                Intent(this, Landing_page::class.java).putExtra("email", email)
                            )
                            finish()
                        } else {
                            Toast.makeText(this, "Wrong credentials", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "All fields must be filled!", Toast.LENGTH_LONG).show()
            }
        }

        binding.signup.setOnClickListener {
            startActivity(Intent(this, Signup_Page::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }


    }
    @SuppressLint("ClickableViewAccessibility")
    fun EditText.setupPasswordVisibilityToggle() {
        val drawableEnd: Drawable? = compoundDrawablesRelative[2] // Assuming the drawableEnd is set at index 2

        drawableEnd?.let {
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= (right - it.bounds.width())) {
                        // Toggle password visibility
                        val isVisible = transformationMethod == PasswordTransformationMethod.getInstance()
                        val newTransformationMethod = if (isVisible) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
                        setTransformationMethod(newTransformationMethod)
                        setSelection(text.length)
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }
    }

    fun forgotPasswordClicked(view: View) {
        val intent = Intent(this, Pass_reset::class.java)
        startActivity(intent)
    }

    private fun check(): Boolean {
        val email = binding.emailll.text.toString().trim()
        val password = binding.pass.text.toString().trim()
        return email.isNotEmpty() && password.isNotEmpty()
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleGoogleSignInResult(task)
            }
        }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account = task.result
            if (account != null) {
                firebaseAuthWithGoogle(account)
            }
        } else {
            Toast.makeText(this, "Google sign-in failed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        Log.d("LoginScreenActivity", "firebaseAuthWithGoogle: starting")
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credentials)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("LoginScreenActivity", "firebaseAuthWithGoogle: success")
                    startActivity(Intent(this, Landing_page::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()

                } else {
                    Log.d("LoginScreenActivity", "firebaseAuthWithGoogle: failed")
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
    fun resetPasswordClicked(view: View) {


        val email = binding.emailll.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
            return
        }

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
}
