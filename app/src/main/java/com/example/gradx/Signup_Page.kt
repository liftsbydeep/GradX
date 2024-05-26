package com.example.gradx
// In signup.kt
// In signup.kt


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
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
import com.google.firebase.firestore.FirebaseFirestore

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

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        progressBar = binding.progressBar5
        val Dialog_Box = Dialog_Box()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googlesignup.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            signInWithGoogle()
        }
// In signup.kt
        val dialogBox = Dialog_Box()

        binding.signupbtn.setOnClickListener {
            if (check()) {
                progressBar.visibility = View.VISIBLE
                val Email = binding.emailll.text.toString().trim()
                val Password = binding.passs.text.toString().trim()
                val Name = binding.name.editText?.text.toString().trim()
                val Pnumber = binding.phonenumber.editText?.text.toString().trim()
                binding.cnfpass.editText?.text.toString().trim()
                val user = hashMapOf(
                    "Name" to Name,
                    "Phone" to Pnumber,
                    "Email" to Email
                )
                val Users = db.collection("USERS")
                val query = Users.whereEqualTo("Email", Email).get()
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

    private fun check(): Boolean {
        val name = binding.name.editText?.text.toString().trim()
        val pnumber = binding.phonenumber.editText?.text.toString().trim()
        val email = binding.emailll.text.toString().trim()
        val password = binding.passs.text.toString().trim()
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
