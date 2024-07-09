package com.example.gradx

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.activity.result.ActivityResult
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
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.passa.setupPasswordVisibilityToggle()
        binding.cnfpasss.setupPasswordVisibilityToggle()
        auth = Firebase.auth
        db = Firebase.firestore

        progressBar = binding.progressBar5

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("GradxPrefs", Context.MODE_PRIVATE)

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

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val documents = db.collection("USERS").whereEqualTo("Email", email).get().await()
                        if (documents.isEmpty) {
                            val isUserCreated = createUserWithEmailAndPassword(email, password, user)
                            handleUserCreationResult(isUserCreated, email, name)
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
        val drawableEnd: Drawable? = compoundDrawablesRelative[2]

        drawableEnd?.let {
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= (right - it.bounds.width())) {
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

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign-in failed.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Google sign-in failed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val idToken = account.idToken
        if (idToken != null) {
            val credentials = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credentials)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        Log.d("Signup_Page", "Google sign-in successful. User: ${user?.email}")
                        user?.let {
                            val userData = hashMapOf(
                                "Name" to it.displayName,
                                "Email" to it.email,
                                "profileImageUrl" to (it.photoUrl?.toString() ?: ""),
                                "Phone" to (it.phoneNumber ?: "")
                            )
                            createUserDocumentInFirestore(it.email ?: "", userData)
                        } ?: Log.e("Signup_Page", "User object is null after successful sign-in")

                        // Move these lines inside the user?.let block
                        saveUserLoginState(user?.email)
                        startActivity(Intent(this, LandingPage::class.java).apply {
                            putExtra("USER_NAME", user?.displayName)
                        })
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    } else {
                        Log.e("Signup_Page", "Google sign-in failed", task.exception)
                        Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                    progressBar.visibility = View.GONE
                }
        } else {
            Log.e("Signup_Page", "ID token is null")
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Google sign-in failed: ID token is null", Toast.LENGTH_SHORT).show()
        }
    }
    private fun createUserDocumentInFirestore(email: String, userData: HashMap<String, String?>) {
        if (email.isEmpty()) {
            Log.e("Signup_Page", "Cannot create user document: Email is empty")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userDocument = db.collection("USERS").document(email)
                val documentSnapshot = userDocument.get().await()

                if (!documentSnapshot.exists()) {
                    userDocument.set(userData).await()
                    Log.d("Signup_Page", "User document created in Firestore for email: $email")
                } else {
                    userDocument.update(userData as Map<String, Any>).await()
                    Log.d("Signup_Page", "User document updated in Firestore for email: $email")
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Signup_Page, "User registered successfully", Toast.LENGTH_SHORT).show()
                    saveUserLoginState(email)
                    startActivity(Intent(this@Signup_Page, LandingPage::class.java).apply {
                        putExtra("USER_NAME", userData["Name"])
                    })
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            } catch (e: Exception) {
                Log.e("Signup_Page", "Error creating/updating user document for email $email", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Signup_Page, "Failed to register user: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun createUserWithEmailAndPassword(email: String, password: String, user: Map<String, String>): Boolean {
        return try {
            val task = auth.createUserWithEmailAndPassword(email, password).await()
            if (task.user != null) {
                val userWithPhoto = user.toMutableMap()
                userWithPhoto["profileImageUrl"] = ""
                db.collection("USERS").document(email).set(userWithPhoto).await()
                true
            } else {
                false
            }
        } catch (exception: Exception) {
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                showAlertDialog("Alert", exception.message ?: "Unknown Error")
            }
            false
        }
    }

    private suspend fun handleUserCreationResult(isSuccessful: Boolean, email: String, name: String) {
        withContext(Dispatchers.Main) {
            progressBar.visibility = View.GONE
            if (isSuccessful) {
                saveUserLoginState(email)

                startActivity(Intent(this@Signup_Page, LandingPage::class.java).apply {
                    putExtra("USER_NAME", name)
                })

                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            } else {
                showAlertDialog("Alert", "User registration failed. Please try again.")
            }
        }
    }

    private fun saveUserLoginState(email: String?) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("IS_LOGGED_IN", true)
        editor.putString("USER_EMAIL", email)
        editor.apply()
    }

    private fun Context.showAlertDialog(title: String, message: String) {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.custom_dialog_box, null)

        val dtitle = view.findViewById<TextView>(R.id.dialogTitle)
        val dmessage = view.findViewById<TextView>(R.id.dialogMessage)
        val dbtn = view.findViewById<Button>(R.id.dialogButton)

        dtitle.text = title
        dmessage.text = message

        val builder = AlertDialog.Builder(this)
        builder.setView(view)
        val dialog = builder.create()

        dbtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
