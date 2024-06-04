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
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
                val email = binding.emaillll.text.toString().trim()
                val password = binding.passa.text.toString().trim()
                val name = binding.name.text.toString().trim()
                val phoneNumber = binding.phonenumber.text.toString().trim()

                val user = hashMapOf(
                    "Name" to name,
                    "Phone" to phoneNumber,
                    "Email" to email
                )

                // Call the createUserWithEmailAndPassword function
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val documents = db.collection("USERS").whereEqualTo("Email", email).get().await()
                        if (documents.isEmpty) {
                            val isUserCreated = createUserWithEmailAndPassword(email, password, user)
                            handleUserCreationResult(isUserCreated, name)
                        } else {
                            withContext(Dispatchers.Main) {
                                showAlertDialog("Alert", "User already exists")
                            }
                        }
                    } catch (exception: Exception) {
                        withContext(Dispatchers.Main) {
                            showAlertDialog("Error", exception.message ?: "Unknown Error")
                        }
                    } finally {
                        withContext(Dispatchers.Main) {
                            progressBar.visibility = View.GONE
                        }
                    }
                }
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
                        transformationMethod = newTransformationMethod
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
            val account = task.getResult(ApiException::class.java)
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

    private suspend fun createUserWithEmailAndPassword(email: String, password: String, user: Map<String, String>): Boolean {
        return try {
            val documents = db.collection("USERS").whereEqualTo("Email", email).get().await()
            if (documents.isEmpty) {
               // db.disableNetwork().await() // Disable network to prevent interference with other clients
                val task = auth.createUserWithEmailAndPassword(email, password).await()
                if (task.user != null) {
                    db.collection("USERS").document(email).set(user).await()
                 //   db.enableNetwork().await() // Re-enable network after Firestore operation
                    true
                } else {
                  //  db.enableNetwork().await() // Re-enable network in case of failure
                    false
                }
            } else {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@Signup_Page, "User Already Registered", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@Signup_Page, Login_Page::class.java))
                }
                false
            }
        } catch (exception: Exception) {
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                showAlertDialog("Alert", exception.message ?: "Unknown Error")

            }
         //   db.enableNetwork().await()    // Re-enable network in case of exception
            false
        }
    }


    private suspend fun handleUserCreationResult(isSuccessful: Boolean, name: String) {
        withContext(Dispatchers.Main) {
            progressBar.visibility = View.GONE
            if (isSuccessful) {
                startActivity(Intent(this@Signup_Page, Landing_page::class.java).putExtra("name", name))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            } else {
                showAlertDialog("Alert", "User registration failed. Please try again.")
            }
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
