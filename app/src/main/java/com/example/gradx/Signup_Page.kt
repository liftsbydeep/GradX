package com.example.gradx

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.gradx.databinding.ActivitySignupPageBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Signup_Page : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignupPageBinding

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.passa.setupPasswordVisibilityToggle()

        // Call setupPasswordVisibilityToggle() for cnfpasss EditText
        binding.cnfpasss.setupPasswordVisibilityToggle()
        auth = Firebase.auth
        db = Firebase.firestore
        progressBar = binding.progressBar5

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googlesignup.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            signInWithGoogle()
        }

        binding.signupbtn.setOnClickListener {
            if (check()) {
                progressBar.visibility = View.VISIBLE
                val Email = binding.emaillll.text.toString().trim()
                val Password = binding.passa.text.toString().trim()
                val Name = binding.name.text.toString().trim()
                val Pnumber = binding.phonenumber.text.toString().trim()
                binding.cnfpasss.text.toString().trim()
                val user = hashMapOf(
                    "Name" to Name,
                    "Phone" to Pnumber,
                    "Email" to Email
                )
                val Users = db.collection("USERS")
                Users.whereEqualTo("Email", Email).get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            auth.createUserWithEmailAndPassword(Email, Password)
                                .addOnCompleteListener(this) { task ->
                                    progressBar.visibility = View.GONE
                                    if (task.isSuccessful) {
                                        Users.document(Email).set(user)
                                        startActivity(Intent(this, Landing_page::class.java).putExtra("name", Name))
                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                                        finish()
                                    } else {
                                        showAlertDialog("Alert", task.exception?.message ?: "Unknown Error")
                                    }
                                }
                        } else {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, "User Already Registered", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, Landing_page::class.java))
                        }
                    }
                    .addOnFailureListener { exception ->
                        progressBar.visibility = View.GONE
                        showAlertDialog("Alert", exception.message ?: "Unknown Error")
                    }
            } else {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Wrong credentials", Toast.LENGTH_LONG).show()
            }
        }

        binding.backtologin.setOnClickListener {
            startActivity(Intent(this, Login_Page::class.java))
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

    private fun check(): Boolean {
        val name = binding.name.text.toString().trim()
        val pnumber = binding.phonenumber.text.toString().trim()
        val email = binding.emaillll.text.toString().trim()
        val password = binding.passa.text.toString().trim()
        val confirmPassword = binding.cnfpasss.text.toString().trim()

        return when {
            name.isEmpty() || pnumber.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
                false
            }
            password != confirmPassword -> {
                Toast.makeText(this, "Password mismatch", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
        val idToken = account.idToken
        if (idToken != null) {
            val credentials = GoogleAuthProvider.getCredential(idToken, null)
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
        } else {
            // Handle case where ID token is null
            Log.e("LoginScreenActivity", "ID token is null")
        }
    }


    private fun Context.showAlertDialog(title: String, message: String) {
        // Inflate the custom layout/view
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.custom_dialog_box, null)

        // Find views in the custom layout
        val dtitle = view.findViewById<TextView>(R.id.dialogTitle)
        val dmessage = view.findViewById<TextView>(R.id.dialogMessage)
        val dbtn = view.findViewById<Button>(R.id.dialogButton)

        // Set the text for title and message
        dtitle.text = title
        dmessage.text = message

        // Create the AlertDialog with the custom view
        val builder = AlertDialog.Builder(this)
        builder.setView(view)
        val dialog = builder.create()

        // Set click listener for the button
        dbtn.setOnClickListener {
            dialog.dismiss() // Dismiss the dialog when button is clicked
        }

        // Show the dialog
        dialog.show()
    }
}
